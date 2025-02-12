package org.kafka.evrak.enums;

import lombok.Getter;

@Getter
public enum DocumentCategory {

    GELEN("GELEN"),
    GİDEN("GİDEN");

    private final String extension;

    DocumentCategory(String extension) {
        this.extension = extension;
    }
}
