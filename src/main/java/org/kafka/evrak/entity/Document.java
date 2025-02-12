package org.kafka.evrak.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kafka.evrak.enums.DocumentCategory;
import org.kafka.evrak.enums.DocumentFlow;


@Entity
@Table(name = "document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document extends BaseEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private DocumentFlow type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 5)
    private DocumentCategory category;

    @Column(name = "description")
    private String description;
}
