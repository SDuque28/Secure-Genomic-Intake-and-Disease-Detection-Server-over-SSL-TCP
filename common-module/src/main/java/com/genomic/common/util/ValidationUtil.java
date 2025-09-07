package com.genomic.common.util;

import com.genomic.common.ProtocolException;
import com.genomic.common.ProtocolConstants;
import org.json.JSONObject;

/**
 * ValidationUtil - Provides validation methods for genomic data and patient metadata
 * Ensures data integrity and compliance with system requirements before processing
 */
public class ValidationUtil {

    /**
     * Validates patient metadata for required fields and format constraints
     * Ensures all required fields are present and contain valid values
     *
     * @param metadata JSON object containing patient demographic and clinical information
     * @throws ProtocolException if any validation rule is violated
     */
    public static void validatePatientMetadata(JSONObject metadata) throws ProtocolException {
        if (metadata == null) {
            throw new ProtocolException("Metadata cannot be null", ProtocolConstants.ERR_INVALID_FORMAT);
        }

        // Required fields validation
        if (!metadata.has("fullName") || metadata.getString("fullName").trim().isEmpty()) {
            throw new ProtocolException("Full name is required", ProtocolConstants.ERR_INVALID_FORMAT);
        }

        if (!metadata.has("documentId") || metadata.getString("documentId").trim().isEmpty()) {
            throw new ProtocolException("Document ID is required", ProtocolConstants.ERR_INVALID_FORMAT);
        }

        if (!metadata.has("age") || metadata.getInt("age") <= 0 || metadata.getInt("age") > 150) {
            throw new ProtocolException("Valid age is required (1-150)", ProtocolConstants.ERR_INVALID_FORMAT);
        }

        if (!metadata.has("sex") || !metadata.getString("sex").matches("^[MF]$")) {
            throw new ProtocolException("Sex must be 'M' or 'F'", ProtocolConstants.ERR_INVALID_FORMAT);
        }

        if (!metadata.has("email") || !isValidEmail(metadata.getString("email"))) {
            throw new ProtocolException("Valid email is required", ProtocolConstants.ERR_INVALID_FORMAT);
        }
    }
    private static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Validates file size against maximum allowed limit
     * Prevents excessively large files from overwhelming system resources
     * @param fileSizeBytes size of the file in bytes
     * @throws ProtocolException if file size exceeds maximum limit
     */
    public static void validateFileSize(long fileSizeBytes) throws ProtocolException {
        long maxSize = 10 * 1024 * 1024; // 10MB limit
        if (fileSizeBytes > maxSize) {
            throw new ProtocolException("File size exceeds maximum limit of 10MB",
                    ProtocolConstants.ERR_INVALID_FORMAT);
        }
    }
}
