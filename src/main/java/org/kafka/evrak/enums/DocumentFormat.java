package org.kafka.evrak.enums;

import lombok.Getter;

/**
 * DocumentFormat, desteklenen doküman dosya formatlarını belirtir.
 */
@Getter
public enum DocumentFormat {

    PDF(".pdf"),
    DOC(".doc"),
    DOCX(".docx"),
    XLS(".xls"),
    XLSX(".xlsx"),
    PPT(".ppt"),
    PPTX(".pptx"),
    TXT(".txt"),
    CSV(".csv"),
    RTF(".rtf"),
    ODT(".odt"),
    HTML(".html"),
    XML(".xml"),
    JSON(".json"),
    ZIP(".zip"),
    RAR(".rar"),
    TAR(".tar"),
    GZ(".gz"),
    JPEG(".jpeg"),
    JPG(".jpg"),
    PNG(".png"),
    GIF(".gif"),
    BMP(".bmp"),
    SVG(".svg");

    private final String extension;

    DocumentFormat(String extension) {
        this.extension = extension;
    }
}
