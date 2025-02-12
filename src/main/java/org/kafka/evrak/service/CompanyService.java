package org.kafka.evrak.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.kafka.evrak.dto.request.DtoCompanyIU;
import org.kafka.evrak.dto.response.DtoCompany;
import org.kafka.evrak.entity.Company;
import org.kafka.evrak.exception.BaseException;
import org.kafka.evrak.exception.ErrorMessage;
import org.kafka.evrak.exception.MessageType;
import org.kafka.evrak.mapper.CompanyMapper;
import org.kafka.evrak.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    // Base directory for company folders
    private static final Path UPLOADS_DIR = Paths.get("uploads");

    /**
     * Yardımcı metod: Belirtilen firma adına göre klasör oluşturur.
     */
    private String createCompanyFolder(String companyName) {
        try {
            Path companyFolder = UPLOADS_DIR.resolve(companyName);
            Files.createDirectories(companyFolder);
            return companyFolder.toString();
        } catch (IOException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.FOLDER_CREATION_FAILED,
                    "Folder creation error | Company: " + companyName + " | Details: " + e.getMessage()
            ));
        }
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
                            + " | New name: " + newFolderName + " | Details: " + e.getMessage()
            ));
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

        try {
            Company company = companyMapper.toEntity(dto);
            String folderPath = createCompanyFolder(companyName);
            company.setFolderPath(folderPath);
            Company savedCompany = companyRepository.save(company);
            return companyMapper.toDto(savedCompany);
        } catch (BaseException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.FOLDER_CREATION_FAILED,
                    "Failed to create folder for company: " + companyName));
        }
    }

    /**
     * Firma güncelleme işlemi:
     * - Eğer güncelleme isteğinde gönderilen firma adı mevcut şirketin adıyla aynıysa,
     *   COMPANY_NAME_DUPLICATE hatası fırlatılır.
     * - Farklı bir isim girildiyse, önce duplicate kontrolü yapılır:
     *      - Eğer aynı isimde aktif veya pasif başka bir firma varsa, ilgili hata fırlatılır.
     * - Ardından klasör adı rename edilip, şirketin adı güncellenir.
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

        try {
            String oldFolderPath = company.getFolderPath();
            String newFolderPath = renameFolder(oldFolderPath, newCompanyName);
            company.setFolderPath(newFolderPath);
            company.setName(newCompanyName);
            Company updatedCompany = companyRepository.save(company);
            return companyMapper.toDto(updatedCompany);
        } catch (Exception e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.FOLDER_RENAME_FAILED,
                    "Failed to rename folder from " + company.getFolderPath() + " to " + newCompanyName));
        }
    }


    /**
     * Firma silme (soft delete) işlemi:
     * - Firma isActive false yapılır.
     * - Klasör adının önüne "archived_" eklenir.
     */
    @Transactional
    public void deleteCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Company not found.")));
        if (!company.isActive()) {
            throw new BaseException(new ErrorMessage(
                    MessageType.COMPANY_ALREADY_INACTIVE,
                    "Company is already inactive."));
        }

        try {
            String oldFolderPath = company.getFolderPath();
            Path oldPath = Paths.get(oldFolderPath);
            String folderName = oldPath.getFileName().toString();
            if (!folderName.startsWith("archived_")) {
                String archivedFolderName = "archived_" + folderName;
                String newFolderPath = renameFolder(oldFolderPath, archivedFolderName);
                company.setFolderPath(newFolderPath);
            }
            company.setActive(false);
            companyRepository.save(company);
        } catch (BaseException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.FOLDER_RENAME_FAILED,
                    "Failed to archive folder: " + company.getFolderPath()));
        }
    }

    /**
     * Arşivden firma geri getirme işlemi:
     * - Firma isActive true yapılır.
     * - Klasör adının önündeki "archived_" kaldırılır.
     */
    @Transactional
    public DtoCompany restoreCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Company not found.")));
        if (company.isActive()) {
            throw new BaseException(new ErrorMessage(
                    MessageType.COMPANY_ALREADY_ACTIVE,
                    "Company is already active."));
        }

        try {
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
            return companyMapper.toDto(restoredCompany);
        } catch (BaseException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.FOLDER_RENAME_FAILED,
                    "Failed to restore folder: " + company.getFolderPath()));
        }
    }


    /**
     * Aktif firmaları veritabanından filtreleyerek getirir.
     * Bu metot, sadece isActive = true olan kayıtları çeker.
     */
    @Transactional(readOnly = true)
    public List<DtoCompany> getActiveCompanies() {
        List<Company> companies = companyRepository.findByIsActive(true);
        return companies.stream()
                .map(companyMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Pasif (inaktif) firmaları veritabanından filtreleyerek getirir.
     * Bu metot, sadece isActive = false olan kayıtları çeker.
     */
    @Transactional(readOnly = true)
    public List<DtoCompany> getInactiveCompanies() {
        List<Company> companies = companyRepository.findByIsActive(false);
        return companies.stream()
                .map(companyMapper::toDto)
                .collect(Collectors.toList());
    }
}