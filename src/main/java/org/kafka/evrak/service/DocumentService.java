package org.kafka.evrak.service;

import lombok.RequiredArgsConstructor;
import org.kafka.evrak.config.FileStorageConfig;
import org.kafka.evrak.dto.request.DtoDocumentFilter;
import org.kafka.evrak.dto.request.DtoDocumentIU;
import org.kafka.evrak.dto.response.DtoDocument;
import org.kafka.evrak.entity.Company;
import org.kafka.evrak.entity.Document;
import org.kafka.evrak.enums.DocumentCategory;
import org.kafka.evrak.enums.DocumentFormat;
import org.kafka.evrak.exception.BaseException;
import org.kafka.evrak.exception.ErrorMessage;
import org.kafka.evrak.exception.MessageType;
import org.kafka.evrak.mapper.DocumentMapper;
import org.kafka.evrak.repository.CompanyRepository;
import org.kafka.evrak.repository.DocumentRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CompanyRepository companyRepository;
    private final DocumentMapper documentMapper;
    private final FileStorageConfig fileStorageConfig;

    @Transactional
    public DtoDocument saveDocument(DtoDocumentIU dto, MultipartFile file) {
        // İlgili şirketin varlığını kontrol et.
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Company not found for id: " + dto.getCompanyId())));

        // Şirket klasörünün varlığını kontrol et.
        Path companyFolder = Paths.get(company.getFolderPath());
        if (!Files.exists(companyFolder)) {
            throw new BaseException(new ErrorMessage(
                    MessageType.COMPANY_FOLDER_NOT_FOUND, "Company folder not found. Please add company first."));
        }

        // Dosya adı kontrolü
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BaseException(new ErrorMessage(
                    MessageType.GENERAL_EXCEPTION, "File name is empty."));
        }

        // Benzersiz dosya adı oluştur (UUID + _ + original ad)
        String uniqueNumber = UUID.randomUUID().toString().replace("-", "");
        String storedFilename = uniqueNumber + "_" + originalFilename;

        // Dosyanın uzantısını alıp, DocumentFormat belirleyin.
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw new BaseException(new ErrorMessage(
                    MessageType.GENERAL_EXCEPTION, "File does not have a valid extension."));
        }
        String ext = originalFilename.substring(dotIndex + 1).toUpperCase();
        DocumentFormat format;
        try {
            format = DocumentFormat.valueOf(ext);
        } catch (IllegalArgumentException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.GENERAL_EXCEPTION, "Unsupported file format: " + ext));
        }

        // Dosyayı şirket klasörü altına benzersiz isimle kopyalayın.
        Path targetPath = companyFolder.resolve(storedFilename);
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.FOLDER_CREATION_FAILED, "Failed to store file: " + storedFilename + " | " + e.getMessage()));
        }

        // Document entity'sini oluşturun.
        Document document = documentMapper.toEntity(dto);
        document.setName(storedFilename); // Benzersiz ismi kaydet
        document.setType(format);
        document.setCompany(company);
        Document savedDocument = documentRepository.save(document);
        return documentMapper.toDto(savedDocument);
    }

    /**
     * Belge silme (soft delete) işlemi:
     * - Document entity'sinde isActive false yapılır.
     * - Dosya sisteminde, dosya adının başına "archived_" eklenir.
     */
    @Transactional
    public Long deactivateDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Document not found.")));
        if (!document.isActive()) {
            throw new BaseException(new ErrorMessage(
                    MessageType.DOCUMENT_ALREADY_INACTIVE, "Document is already inactive."));
        }

        Company company = document.getCompany();
        Path filePath = Paths.get(company.getFolderPath()).resolve(document.getName());
        Path archivedPath = filePath.getParent().resolve("archived_" + filePath.getFileName().toString());
        try {
            Files.move(filePath, archivedPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.FOLDER_RENAME_FAILED, "Failed to archive document file: " + e.getMessage()));
        }
        document.setActive(false);
        document.setName("archived_" + document.getName());
        Document savedDocument = documentRepository.save(document);
        return savedDocument.getId();
    }

    /**
     * Belge geri getirme (restore) işlemi:
     * - Document entity'sinde isActive true yapılır.
     * - Dosya sisteminde, dosya adının başındaki "archived_" kaldırılır.
     */
    @Transactional
    public Long activateDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Document not found.")));
        if (document.isActive()) {
            throw new BaseException(new ErrorMessage(
                    MessageType.DOCUMENT_ALREADY_ACTIVE, "Document is already active."));
        }

        Company company = document.getCompany();
        Path filePath = Paths.get(company.getFolderPath()).resolve(document.getName());
        String currentName = filePath.getFileName().toString();
        if (!currentName.startsWith("archived_")) {
            throw new BaseException(new ErrorMessage(
                    MessageType.GENERAL_EXCEPTION, "Document file does not have archived prefix."));
        }
        String restoredName = currentName.substring("archived_".length());
        Path restoredPath = filePath.getParent().resolve(restoredName);
        try {
            Files.move(filePath, restoredPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.FOLDER_RENAME_FAILED, "Failed to restore document file: " + e.getMessage()));
        }
        document.setActive(true);
        document.setName(restoredName);
        Document savedDocument = documentRepository.save(document);
        return savedDocument.getId();
    }

    /**
     * Belirli bir şirketin aktif belgelerini, filtre kriterlerine göre getirir.
     * Filtreleme: name (kısmı), createdAt aralığı, category (GELEN/GIDEN).
     * Sonuçlar id'ye göre DESC sıralanır.
     */
    @Transactional(readOnly = true)
    public Page<DtoDocument> filterActiveDocuments(DtoDocumentFilter filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Document> documentPage = documentRepository.filterDocuments(
                filter.getCompanyId(),
                true,
                filter.getName(),
                filter.getStartDate(),
                filter.getEndDate(),
                getDocumentCategory(filter.getCategory()),
                pageable
        );
        return documentPage.map(documentMapper::toDto);
    }

    /**
     * Belirli bir şirketin pasif belgelerini, filtre kriterlerine göre getirir.
     * Filtreleme: name (kısmı), createdAt aralığı, category (GELEN/GIDEN).
     * Sonuçlar id'ye göre DESC sıralanır.
     */
    @Transactional(readOnly = true)
    public Page<DtoDocument> filterInactiveDocuments(DtoDocumentFilter filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Document> documentPage = documentRepository.filterDocuments(
                filter.getCompanyId(),
                false,
                filter.getName(),
                filter.getStartDate(),
                filter.getEndDate(),
                getDocumentCategory(filter.getCategory()),
                pageable
        );
        return documentPage.map(documentMapper::toDto);
    }


    /**
     * Belge ID'sine göre dosya (file) bilgisini Resource olarak döner.
     * Bu metot, dosya sistemindeki belge dosyasını erişime açar.
     */
    @Transactional(readOnly = true)
    public Resource getDocumentFile(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(
                        MessageType.NO_RECORD_EXIST, "Document not found.")));
        Company company = document.getCompany();
        Path filePath = Paths.get(company.getFolderPath()).resolve(document.getName());
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BaseException(new ErrorMessage(
                        MessageType.GENERAL_EXCEPTION, "File not found or not readable."));
            }
        } catch (IOException e) {
            throw new BaseException(new ErrorMessage(
                    MessageType.GENERAL_EXCEPTION, "Error reading file: " + e.getMessage()));
        }
    }

    // Yardımcı metod: Gelen category string'ini DocumentCategory enum'ına dönüştürür.
// Eğer geçerli (GELEN veya GIDEN) değilse hata fırlatır.
    private DocumentCategory getDocumentCategory(String categoryStr) {
        if (categoryStr == null || categoryStr.trim().isEmpty()) {
            throw new BaseException(new ErrorMessage(
                    MessageType.DOCUMENT_CATEGORY_INVALID, "Document category must be provided (GELEN/GIDEN)."));
        }
        try {
            return DocumentCategory.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BaseException(new ErrorMessage(
                    MessageType.DOCUMENT_CATEGORY_INVALID, "Invalid document category provided. Must be GELEN or GIDEN."));
        }
    }

}
