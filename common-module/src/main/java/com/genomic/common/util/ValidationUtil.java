package com.genomic.common.util;

import com.genomic.common.ProtocolException;
import com.genomic.common.ProtocolConstants;
import org.json.JSONObject;

public class ValidationUtil {

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

    public static void validateFileSize(long fileSizeBytes) throws ProtocolException {
        long maxSize = 10 * 1024 * 1024; // 10MB limit
        if (fileSizeBytes > maxSize) {
            throw new ProtocolException("File size exceeds maximum limit of 10MB",
                    ProtocolConstants.ERR_INVALID_FORMAT);
        }
    }
}
