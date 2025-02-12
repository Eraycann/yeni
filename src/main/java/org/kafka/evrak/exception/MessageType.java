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

    // Klasör İşlemleri (3000 serisi)
    FOLDER_CREATION_FAILED("3000", "Sistem klasörü oluşturulamadı. Lütfen dizin izinlerini kontrol edin."),
    FOLDER_RENAME_FAILED("3001", "Klasör adı değiştirilemedi. Hedef klasör kullanımda veya izin yetersiz."),

    // Auth & Token Hataları (8000 serisi)
    TOKEN_IS_EXPIRED("8000", "Tokenın süresi bitmiştir"),
    USERNAME_NOT_FOUND("8001", "Username bulunamadı"),
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
