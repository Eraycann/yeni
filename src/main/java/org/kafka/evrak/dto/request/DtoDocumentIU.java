package org.kafka.evrak.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.kafka.evrak.enums.DocumentCategory;

@Data
public class DtoDocumentIU {

    @NotBlank(message = "Doküman adı boş olamaz")
    @Size(max = 100, message = "Doküman adı en fazla 100 karakter olmalıdır")
    private String name;

    @NotNull(message = "Kategori belirtilmelidir")
    private DocumentCategory category;

    @Size(max = 500, message = "Açıklama en fazla 250 karakter olabilir")
    private String description;

    @NotNull(message = "Şirket ID'si boş olamaz")
    private Long companyId;
}