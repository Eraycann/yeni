package org.kafka.evrak.dto.response;

import lombok.Data;
import org.kafka.evrak.enums.DocumentCategory;
import org.kafka.evrak.enums.DocumentFormat;

import java.time.LocalDateTime;

@Data
public class DtoDocument {

    private Long id;

    private LocalDateTime createdAt;

    private DocumentFormat type;

    private DocumentCategory category;

    private String description;

    private DtoCompany dtoCompany;
}
