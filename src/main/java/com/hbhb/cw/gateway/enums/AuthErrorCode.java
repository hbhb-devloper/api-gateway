package com.hbhb.cw.gateway.enums;

import lombok.Getter;

/**
 * @author xiaokang
 * @since 2020-10-06
 */
@Getter
public enum AuthErrorCode {

    USER_ACCESS_UNAUTHORIZED("A0000", "user.access.unauthorized"),

    TOKEN_INVALID("A0001", "token.invalid"),
    TOKEN_EXPIRED("A0002", "token.expired"),

    ;

    private final String code;

    private final String message;

    AuthErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
