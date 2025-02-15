package org.kafka.evrak.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kafka.evrak.dto.request.DtoDocumentFilter;
import org.kafka.evrak.dto.request.DtoDocumentIU;
import org.kafka.evrak.dto.response.DtoDocument;
import org.kafka.evrak.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/documents")
public class RestDocumentController extends RestBaseController {

    private final DocumentService documentService;

    /**
     * Belge kaydı oluşturur.
     * İstek, multipart/form-data formatında "document" (JSON kısmı: DtoDocumentIU)
     * ve "file" (MultipartFile) olarak gönderilmelidir.
     */
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RootEntity<DtoDocument> saveDocument(
            @RequestPart("document") @Valid DtoDocumentIU documentIU,
            @RequestPart("file") MultipartFile file) {
        return ok(documentService.saveDocument(documentIU, file));
    }

    /**
     * Belgeyi soft delete (deactivate) eder.
     */
    @PutMapping("/deactivate/{id}")
    public RootEntity<Long> deactivateDocument(@PathVariable Long id) {
        return ok(documentService.deactivateDocument(id));
    }

    /**
     * Belgeyi geri getirme (activate) işlemi yapar.
     */
    @PutMapping("/activate/{id}")
    public RootEntity<Long> activateDocument(@PathVariable Long id) {
        return ok(documentService.activateDocument(id));
    }

    /**
     * Belgeyi kalıcı olarak siler.
     * Dosya sisteminden dosyayı kaldırır ve veritabanından kaydı siler.
     */
    @DeleteMapping("/delete/{id}")
    public RootEntity<Long> deleteDocumentPermanently(@PathVariable Long id) {
        return ok(documentService.deleteDocumentPermanently(id));
    }

    /**
     * Belirli bir şirketin aktif belgelerini, filtre kriterlerine göre getirir.
     * Filtre kriterleri: name (partial match), createdAt aralığı, category (GELEN/GIDEN).
     * Sonuçlar id'ye göre DESC sıralanır.
     */
    @GetMapping("/filter/active")
    public RootEntity<Page<DtoDocument>> filterActiveDocuments(
            @Valid DtoDocumentFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ok(documentService.filterActiveDocuments(filter, page, size));
    }

    /**
     * Belirli bir şirketin pasif belgelerini, filtre kriterlerine göre getirir.
     * Filtre kriterleri: name (partial match), createdAt aralığı, category (GELEN/GIDEN).
     * Sonuçlar id'ye göre DESC sıralanır.
     */
    @GetMapping("/filter/inactive")
    public RootEntity<Page<DtoDocument>> filterInactiveDocuments(
            @Valid DtoDocumentFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ok(documentService.filterInactiveDocuments(filter, page, size));
    }

    /**
     * Belge ID'sine göre dosya (file) bilgisini Resource olarak döner.
     * Dosya indirme işlemi için gerekli HTTP header ayarları yapılır.
     */
    @GetMapping("/file/{id}")
    public ResponseEntity<Resource> getDocumentFile(@PathVariable Long id) {
        Resource resource = documentService.getDocumentFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Bir şirkete ait TÜM AKTİF belgeleri kalıcı olarak siler.
     */
    @DeleteMapping("/delete-all-active/{companyId}")
    public RootEntity<Integer> deleteAllActiveDocuments(@PathVariable Long companyId) {
        return ok(documentService.deleteAllActiveDocuments(companyId));
    }

    /**
     * Bir şirkete ait TÜM PASİF belgeleri kalıcı olarak siler.
     */
    @DeleteMapping("/delete-all-inactive/{companyId}")
    public RootEntity<Integer> deleteAllInactiveDocuments(@PathVariable Long companyId) {
        return ok(documentService.deleteAllInactiveDocuments(companyId));
    }
}
