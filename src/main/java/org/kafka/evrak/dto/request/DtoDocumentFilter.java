package org.kafka.evrak.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class DtoDocumentFilter {

    @NotNull(message = "Company ID is required.")
    private Long companyId;

    // Kısmı eşleşme için opsiyonel
    private String name;

    // Tarih filtreleri
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    @NotBlank(message = "Document category must be provided (GELEN/GIDEN).")
    private String category;
}
