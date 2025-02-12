package org.kafka.evrak.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DtoCompany {

    private Long id;

    private LocalDateTime createdAt;

    private String name;
}
