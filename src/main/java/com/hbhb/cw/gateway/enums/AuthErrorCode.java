package com.hbhb.cw.gateway.enums;

import lombok.Getter;

/**
 * @author xiaokang
 * @since 2020-10-06
 */
@Getter
public enum AuthErrorCode {

//    AUTH_ERROR_LOGIN_INCORRECT(90001, "auth.error.login.incorrect"),
//    AUTH_ERROR_LOGIN_WITHOUT(90002, "auth.error.login.without"),
//    AUTH_ERROR_TOKEN_MISSING(90003, "auth.error.token.missing"),
//    AUTH_ERROR_TOKEN_EXPIRED(90004, "auth.error.token.expired"),
//    AUTH_ERROR_TOKEN_INVALID(90005, "auth.error.token.invalid"),

    TOKEN_INVALID_OR_EXPIRED("A0230", "token无效或已过期"),

    USER_ACCESS_UNAUTHORIZED("A0301", "访问未授权"),

    ;

    private final String code;

    private final String message;

    AuthErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
