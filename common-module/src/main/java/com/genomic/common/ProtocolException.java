package com.genomic.common;

import lombok.Getter;

@Getter
public class ProtocolException extends Exception {
    private final String errorCode;

    public ProtocolException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ProtocolResponse toResponse() {
        return ProtocolResponse.error(errorCode, getMessage());
    }
}