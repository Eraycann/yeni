package org.kafka.evrak.service;

import lombok.RequiredArgsConstructor;
import org.kafka.evrak.config.FileStorageConfig;
import org.kafka.evrak.dto.request.DtoCompanyIU;
import org.kafka.evrak.dto.response.DtoCompany;
import org.kafka.evrak.entity.Company;
import org.kafka.evrak.entity.Document;
import org.kafka.evrak.exception.BaseException;
import org.kafka.evrak.exception.ErrorMessage;
import org.kafka.evrak.exception.MessageType;
import org.kafka.evrak.mapper.CompanyMapper;
import org.kafka.evrak.repository.CompanyRepository;
import org.kafka.evrak.repository.DocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final FileStorageConfig fileStorageConfig;

    // DocumentRepository'yi de enjekte ediyoruz.
    private final DocumentRepository documentRepository;

    // Base directory for company folders (application.properties'tan alınıyor)
    private Path getUploadsDir() {
        return fileStorageConfig.getUploadsPath();
    }

    /**
     * Yardımcı metod: Belirtilen firma adına göre klasör oluşturur.
     */
    private String createCompanyFolder(String companyName) {
        try {
            Path companyFolder = getUploadsDir().resolve(companyName);
            Files.createDirectories(companyFolder);
            return companyFolder.toString();
        } catch (IOException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.FOLDER_CREATION_FAILED,
                    "Folder creation error | Company: " + companyName + " | Details: " + e.getMessage()));
        }
    }

    /**
     * Yeni firma kaydı oluşturur.
     * Aynı isimde aktif ya da pasif firma varsa hata fırlatılır.
     * Kayıt sonrası "uploads" klasörü altında firma adına göre alt klasör oluşturulur.
     */
    @Transactional
    public DtoCompany saveCompany(DtoCompanyIU dto) {
        String companyName = dto.getName();

        // Aktif firma kontrolü
        if (companyRepository.existsByNameAndIsActive(companyName, true)) {
            throw new BaseException(new ErrorMessage(
                    MessageType.ACTIVE_COMPANY_ALREADY_EXISTS,
                    "Active company with name '" + companyName + "' already exists."));
        }
        // Pasif firma kontrolü
        if (companyRepository.existsByNameAndIsActive(companyName, false)) {
            throw new BaseException(new ErrorMessage(
                    MessageType.INACTIVE_COMPANY_ALREADY_EXISTS,
                    "Inactive company with name '" + companyName + "' already exists."));
        }

        Company company = companyMapper.toEntity(dto);
        String folderPath = createCompanyFolder(companyName);
        company.setFolderPath(folderPath);
        Company savedCompany = companyRepository.save(company);
        return companyMapper.toDto(savedCompany);
    }

    /**
     * Firma güncelleme işlemi:
     * - Eğer güncelleme isteğinde gönderilen firma adı, mevcut şirketin adıyla aynıysa hata fırlatılır.
     * - Farklı bir isim girildiyse, önce duplicate kontrolü yapılır; eğer aynı isimde aktif veya pasif firma varsa hata fırlatılır.
     * - Ardından klasör adı yeniden adlandırılır ve şirketin adı güncellenir.
     */
    @Transactional
    public DtoCompany updateCompany(Long companyId, DtoCompanyIU dto) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Company not found.")));

        String newCompanyName = dto.getName();

        // Eğer güncelleme isteğinde firma adı, mevcut kayıtla aynıysa hata fırlatılır.
        if (company.getName().equals(newCompanyName)) {
            throw new BaseException(new ErrorMessage(
                    MessageType.COMPANY_NAME_DUPLICATE,
                    "Company with name '" + newCompanyName + "' already exists. Please provide a different name."));
        }

        // Duplicate kontrolü: aynı isimde aktif veya pasif firma var mı?
        if (companyRepository.existsByNameAndIsActive(newCompanyName, true)) {
            throw new BaseException(new ErrorMessage(
                    MessageType.ACTIVE_COMPANY_ALREADY_EXISTS,
                    "Active company with name '" + newCompanyName + "' already exists."));
        }
        if (companyRepository.existsByNameAndIsActive(newCompanyName, false)) {
            throw new BaseException(new ErrorMessage(
                    MessageType.INACTIVE_COMPANY_ALREADY_EXISTS,
                    "Inactive company with name '" + newCompanyName + "' already exists."));
        }

        // Klasör yeniden adlandırma
        String oldFolderPath = company.getFolderPath();
        String newFolderPath = renameFolder(oldFolderPath, newCompanyName);
        company.setFolderPath(newFolderPath);
        company.setName(newCompanyName);
        Company updatedCompany = companyRepository.save(company);
        return companyMapper.toDto(updatedCompany);
    }

    /**
     * Yardımcı metod: Var olan klasörün adını yenisiyle değiştirir.
     */
    private String renameFolder(String oldFolderPath, String newFolderName) {
        try {
            Path oldPath = Paths.get(oldFolderPath);
            Path newPath = oldPath.getParent().resolve(newFolderName);
            Files.move(oldPath, newPath);
            return newPath.toString();
        } catch (IOException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.FOLDER_RENAME_FAILED,
                    "Folder rename error | Old path: " + oldFolderPath
                            + " | New name: " + newFolderName + " | Details: " + e.getMessage()));
        }
    }

    /**
     * Firma silme (soft delete) işlemi:
     * - Firma isActive false yapılır.
     * - Klasör adının önüne "archived_" eklenir.
     */
    @Transactional
    public Long deactivateCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Company not found.")));
        if (!company.isActive()) {
            throw new BaseException(new ErrorMessage(
                    MessageType.COMPANY_ALREADY_INACTIVE,
                    "Company is already inactive."));
        }

        String oldFolderPath = company.getFolderPath();
        Path oldPath = Paths.get(oldFolderPath);
        String folderName = oldPath.getFileName().toString();
        if (!folderName.startsWith("archived_")) {
            String archivedFolderName = "archived_" + folderName;
            String newFolderPath = renameFolder(oldFolderPath, archivedFolderName);
            company.setFolderPath(newFolderPath);
        }
        company.setActive(false);
        Company savedCompany = companyRepository.save(company);
        return savedCompany.getId();
    }

    /**
     * Arşivden firma geri getirme işlemi:
     * - Firma isActive true yapılır.
     * - Klasör adının önündeki "archived_" kaldırılır.
     */
    @Transactional
    public Long activateCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Company not found.")));
        if (company.isActive()) {
            throw new BaseException(new ErrorMessage(
                    MessageType.COMPANY_ALREADY_ACTIVE,
                    "Company is already active."));
        }

        String oldFolderPath = company.getFolderPath();
        Path oldPath = Paths.get(oldFolderPath);
        String folderName = oldPath.getFileName().toString();
        if (folderName.startsWith("archived_")) {
            String restoredFolderName = folderName.substring("archived_".length());
            String newFolderPath = renameFolder(oldFolderPath, restoredFolderName);
            company.setFolderPath(newFolderPath);
        }
        company.setActive(true);
        Company restoredCompany = companyRepository.save(company);
        return restoredCompany.getId();
    }

    /**
     * Aktif firmaları veritabanından DESC sıralı olarak getirir.
     */
    @Transactional(readOnly = true)
    public Page<DtoCompany> getAllActiveCompanies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Company> companyPage = companyRepository.findByIsActive(true, pageable);
        return companyPage.map(companyMapper::toDto);
    }

    /**
     * Pasif (inaktif) firmaları veritabanından DESC sıralı olarak getirir.
     */
    @Transactional(readOnly = true)
    public Page<DtoCompany> getAllInactiveCompanies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Company> companyPage = companyRepository.findByIsActive(false, pageable);
        return companyPage.map(companyMapper::toDto);
    }

    /**
     * İsim'e göre aktif şirketi getirir.
     */
    @Transactional(readOnly = true)
    public DtoCompany getActiveCompaniesByName(String name) {
        Company company = companyRepository.findByNameAndIsActive(name, true)
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Active company with name '" + name + "' not found.")));
        return companyMapper.toDto(company);
    }

    /**
     * İsim'e göre pasif şirketi getirir.
     */
    @Transactional(readOnly = true)
    public DtoCompany getInactiveCompaniesByName(String name) {
        Company company = companyRepository.findByNameAndIsActive(name, false)
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Inactive company with name '" + name + "' not found.")));
        return companyMapper.toDto(company);
    }

    /**
     * Şirketin içerisinde hiç evrak yoksa, firmayı kalıcı olarak siler.
     * - Evrak varsa silme işlemi yapılmaz.
     * - Şirketin dosya sistemi klasörü, "archived_" öneki olsun veya olmasın, silinir.
     */
    @Transactional
    public Long deleteCompanyPermanently(Long companyId) {
        // Şirketi getir
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Company not found.")));

        // Şirkete ait aktif evrakları kontrol et
        List<Document> activeDocs = documentRepository.findByCompanyIdAndIsActive(companyId, true);
        if (!activeDocs.isEmpty()) {
            throw new BaseException(new ErrorMessage(
                    MessageType.COMPANY_CONTAINS_ACTIVE_DOCUMENTS,
                    "Company contains active documents and cannot be deleted permanently."));
        }

        // Şirkete ait pasif evrakları kontrol et
        List<Document> inactiveDocs = documentRepository.findByCompanyIdAndIsActive(companyId, false);
        if (!inactiveDocs.isEmpty()) {
            throw new BaseException(new ErrorMessage(
                    MessageType.COMPANY_CONTAINS_INACTIVE_DOCUMENTS,
                    "Company contains inactive documents and cannot be deleted permanently."));
        }

        // Şirketin dosya sistemindeki klasörünü silmek üzere yolunu belirle
        Path folderPath = Paths.get(company.getFolderPath());
        if (!Files.exists(folderPath)) {
            // Eğer normal klasör bulunamazsa, "archived_" önekli klasörü kontrol et
            Path parent = folderPath.getParent();
            if (parent != null) {
                folderPath = parent.resolve("archived_" + folderPath.getFileName().toString());
            }
            if (!Files.exists(folderPath)) {
                throw new BaseException(new ErrorMessage(
                        MessageType.COMPANY_FOLDER_NOT_FOUND, "Company folder not found in file system."));
            }
        }

        // Klasörün boş olup olmadığını kontrol et
        try (var entries = Files.list(folderPath)) {
            if (entries.findFirst().isPresent()) {
                throw new BaseException(new ErrorMessage(
                        MessageType.COMPANY_FOLDER_NOT_EMPTY,
                        "Company folder is not empty. Deletion aborted for security reasons."));
            }
        } catch (IOException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.FOLDER_RENAME_FAILED,
                    "Failed to inspect company folder: " + e.getMessage()));
        }

        // Klasör boşsa, sil
        try {
            Files.delete(folderPath);
        } catch (IOException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.FOLDER_RENAME_FAILED,
                    "Failed to delete company folder: " + e.getMessage()));
        }

        // Şirketi veritabanından sil
        companyRepository.delete(company);
        return companyId;
    }

}
