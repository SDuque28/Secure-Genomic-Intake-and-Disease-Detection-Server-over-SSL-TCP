package com.genomic.common;

import lombok.Getter;

/**
 * ProtocolException - Custom exception for genomic protocol-related errors
 * Extends Exception to provide structured error handling with error codes
 * Used throughout the genomic system for consistent error reporting
 */
@Getter
public class ProtocolException extends Exception {
    private final String errorCode;

    /**
     * Constructs a new ProtocolException with a descriptive message and error code
     * @param message human-readable error description for logging and display
     * @param errorCode machine-readable error code from ProtocolConstants
     */
    public ProtocolException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Converts the exception into a ProtocolResponse for client communication
     * This allows exceptions to be easily converted into proper protocol responses
     * @return ProtocolResponse containing the error code and message
     */
    public ProtocolResponse toResponse() {
        return ProtocolResponse.error(errorCode, getMessage());
    }
}