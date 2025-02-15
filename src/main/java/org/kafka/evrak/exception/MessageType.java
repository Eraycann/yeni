package org.kafka.evrak.exception;

import lombok.Getter;

@Getter
public enum MessageType {

    // Kayıt Bulunamadı (1000 serisi)
    NO_RECORD_EXIST("1000", "Kayıt bulunamadı"),

    // Şirket İşlemleri (2000 serisi)
    ACTIVE_COMPANY_ALREADY_EXISTS("2000", "Aynı isimde aktif şirket zaten mevcut"),
    INACTIVE_COMPANY_ALREADY_EXISTS("2001", "Aynı isimde pasif şirket zaten mevcut"),
    COMPANY_ALREADY_INACTIVE("2002", "Şirket zaten pasif durumda"),
    COMPANY_ALREADY_ACTIVE("2003", "Şirket zaten aktif durumda"),
    COMPANY_NAME_DUPLICATE("2004", "Firma adı zaten mevcut. Lütfen farklı bir isim giriniz."),

    // Eklenen Şirket Silme Hataları (2005 serisi)
    COMPANY_CONTAINS_ACTIVE_DOCUMENTS("2005", "Şirket aktif belgeler içeriyor ve kalıcı olarak silinemez."),
    COMPANY_CONTAINS_INACTIVE_DOCUMENTS("2006", "Şirket pasif belgeler içeriyor ve kalıcı olarak silinemez."),

    // Klasör İşlemleri (3000 serisi)
    FOLDER_CREATION_FAILED("3000", "Sistem klasörü oluşturulamadı. Lütfen dizin izinlerini kontrol edin."),
    FOLDER_RENAME_FAILED("3001", "Klasör adı değiştirilemedi. Hedef klasör kullanımda veya izin yetersiz."),
    COMPANY_FOLDER_NOT_FOUND("3002", "Şirket klasörü bulunamadı. Lütfen önce şirket ekleyin."),
    COMPANY_FOLDER_NOT_EMPTY("3003", "Şirket klasörü boş değil. Güvenlik nedeniyle silme iptal edildi."),

    // Belge İşlemleri (300 serisi)
    DOCUMENT_ALREADY_ACTIVE("3005", "Belge zaten aktif durumda."),
    DOCUMENT_ALREADY_INACTIVE("3006", "Belge zaten pasif durumda."),
    DOCUMENT_ALREADY_EXISTS("3007", "Bu adla belge şirkette zaten mevcut."),
    DOCUMENT_DELETION_FAILED("3008", "Belge kalıcı olarak silinemedi."),
    FILE_NOT_FOUND("3009", "Dosya bulunamadı."), // Yeni eklenen hata
    FILE_DELETE_FAILED("3010", "Dosya silinirken hata oluştu."), // Yeni eklenen hata
    NO_ACTIVE_DOCUMENTS_FOUND("3011", "No active documents found for this company."),
    NO_INACTIVE_DOCUMENTS_FOUND("3012", "No inactive documents found for this company."),


    // Kategori Hataları
    DOCUMENT_CATEGORY_INVALID("3100", "Geçersiz belge kategorisi sağlandı. GELEN veya GIDEN olmalı."),

    // Auth & Token Hataları (8000 serisi)
    TOKEN_IS_EXPIRED("8000", "Tokenın süresi bitmiştir"),
    USERNAME_NOT_FOUND("8001", "Kullanıcı adı bulunamadı"),
    USERNAME_OR_PASSWORD_INVALID("8002", "Kullanıcı adı veya şifre hatalı"),
    REFRESH_TOKEN_NOT_FOUND("8003", "Refresh token bulunamadı"),
    REFRESH_TOKEN_IS_EXPIRED("8004", "Refresh token süresi bitmiş"),

    // Genel Hata (9999)
    GENERAL_EXCEPTION("9999", "Genel bir hata oluştu");

    private final String code;
    private final String message;

    MessageType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}