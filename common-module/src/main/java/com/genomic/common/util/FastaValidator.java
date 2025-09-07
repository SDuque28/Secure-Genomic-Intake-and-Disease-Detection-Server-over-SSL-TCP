package com.genomic.common.util;

import com.genomic.common.ProtocolException;
import com.genomic.common.ProtocolConstants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * FastaValidator - Utility class for validating and processing FASTA format genomic data
 * Provides methods for format validation, checksum calculation, and metadata extraction
 * Ensures genomic data integrity and compliance with FASTA format specifications
 */
public class FastaValidator {

    /**
     * Validates FASTA format compliance and sequence integrity
     * Checks for proper header format, valid nucleotide characters, and basic structure
     * @param fastaContent the genomic sequence data in FASTA format
     * @throws ProtocolException if the FASTA content violates format requirements
     */
    public static void validateFasta(String fastaContent) throws ProtocolException {
        if (fastaContent == null || fastaContent.trim().isEmpty()) {
            throw new ProtocolException("FASTA content is empty", ProtocolConstants.ERR_INVALID_FASTA);
        }

        String[] lines = fastaContent.split("\n");
        if (lines.length < 2) {
            throw new ProtocolException("FASTA must have at least 2 lines", ProtocolConstants.ERR_INVALID_FASTA);
        }

        // Validate header line
        String header = lines[0].trim();
        if (!header.startsWith(">")) {
            throw new ProtocolException("FASTA header must start with '>'", ProtocolConstants.ERR_INVALID_FASTA);
        }

        if (header.length() == 1) {
            throw new ProtocolException("FASTA header must have an identifier after '>'", ProtocolConstants.ERR_INVALID_FASTA);
        }

        // Validate sequence lines
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.matches("^[ACGTN]+$")) {
                throw new ProtocolException("FASTA sequence contains invalid characters: " + line,
                        ProtocolConstants.ERR_INVALID_FASTA);
            }
        }
    }

    /**
     * Calculates SHA-256 checksum for genomic data integrity verification
     * Used to detect data corruption or tampering during transmission/storage
     * @param content the genomic sequence content to hash
     * @return SHA-256 checksum as a hexadecimal string
     * @throws ProtocolException if SHA-256 algorithm is unavailable
     */
    public static String calculateChecksum(String content) throws ProtocolException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new ProtocolException("Failed to calculate checksum", ProtocolConstants.ERR_SERVER_ERROR);
        }
    }

    /**
     * Extracts the identifier from the FASTA header line
     * Removes the '>' character and trims whitespace
     * @param fastaContent the FASTA format genomic data
     * @return the extracted identifier, or "unknown" if not available
     */
    public static String extractIdentifier(String fastaContent) {
        String[] lines = fastaContent.split("\n");
        if (lines.length > 0) {
            String header = lines[0].trim();
            return header.substring(1).trim(); // Remove '>' and trim
        }
        return "unknown";
    }
}