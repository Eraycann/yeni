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
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION,
                    "Failed to create folder for company: " + companyName));
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
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION,
                    "Failed to rename folder from " + oldFolderPath + " to " + newFolderName));
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
        if (companyRepository.findByNameAndIsActive(companyName, true).isPresent()) {
            throw new BaseException(new ErrorMessage(
                    MessageType.ACTIVE_COMPANY_ALREADY_EXISTS,
                    "Active company with name '" + companyName + "' already exists."));
        }
        // Pasif firma kontrolü
        if (companyRepository.findByNameAndIsActive(companyName, false).isPresent()) {
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
     * - Firma adında değişiklik varsa, duplicate kontrolü yapılır.
     * - Klasör adı da güncellenir.
     */
    @Transactional
    public DtoCompany updateCompany(Long companyId, DtoCompanyIU dto) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Company not found.")));

        String newCompanyName = dto.getName();
        // Aynı isimde başka bir aktif firma varsa
        companyRepository.findByNameAndIsActive(newCompanyName, true)
                .filter(existing -> !existing.getId().equals(companyId))
                .ifPresent(existing -> {
                    throw new BaseException(new ErrorMessage(
                            MessageType.ACTIVE_COMPANY_ALREADY_EXISTS,
                            "Active company with name '" + newCompanyName + "' already exists."));
                });
        // Aynı isimde başka bir pasif firma varsa
        companyRepository.findByNameAndIsActive(newCompanyName, false)
                .filter(existing -> !existing.getId().equals(companyId))
                .ifPresent(existing -> {
                    throw new BaseException(new ErrorMessage(
                            MessageType.INACTIVE_COMPANY_ALREADY_EXISTS,
                            "Inactive company with name '" + newCompanyName + "' already exists."));
                });

        // Eğer firma adında değişiklik varsa klasör yeniden adlandırılır
        if (!company.getName().equals(newCompanyName)) {
            String oldFolderPath = company.getFolderPath();
            String newFolderPath = renameFolder(oldFolderPath, newCompanyName);
            company.setFolderPath(newFolderPath);
            company.setName(newCompanyName);
        }
        Company updatedCompany = companyRepository.save(company);
        return companyMapper.toDto(updatedCompany);
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
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION,
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
        companyRepository.save(company);
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
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION,
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
        return companyMapper.toDto(restoredCompany);
    }

    /**
     * Aktif firmaları getirir.
     */
    @Transactional(readOnly = true)
    public List<DtoCompany> getActiveCompanies() {
        List<Company> companies = companyRepository.findAll()
                .stream()
                .filter(Company::isActive)
                .collect(Collectors.toList());
        return companies.stream()
                .map(companyMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Pasif (inaktif) firmaları getirir.
     */
    @Transactional(readOnly = true)
    public List<DtoCompany> getInactiveCompanies() {
        List<Company> companies = companyRepository.findAll()
                .stream()
                .filter(company -> !company.isActive())
                .collect(Collectors.toList());
        return companies.stream()
                .map(companyMapper::toDto)
                .collect(Collectors.toList());
    }
}