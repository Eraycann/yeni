package org.kafka.evrak.exception;

import lombok.Getter;

@Getter
public enum MessageType {

    NO_RECORD_EXIST("1000", "Kayıt bulunamadı"),

    ACTIVE_COMPANY_ALREADY_EXISTS("2000", "Aynı isimde aktif şirket zaten mevcut"),
    INACTIVE_COMPANY_ALREADY_EXISTS("2001", "Aynı isimde pasif şirket zaten mevcut"),

    TOKEN_IS_EXPIRED("8000", "Tokenın süresi bitmiştir"),
    USERNAME_NOT_FOUND("8001", "Username bulunamadı"),
    USERNAME_OR_PASSWORD_INVALID("8002", "Kullanıcı adı veya şifre hatalı"),
    REFRESH_TOKEN_NOT_FOUND("8003", "Refresh token bulunamadı"),
    REFRESH_TOKEN_IS_EXPIRED("8004", "Refresh token süresi bitmiş"),

    GENERAL_EXCEPTION("9999", "Genel bir hata oluştu");

    private final String code;
    private final String message;

    MessageType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
