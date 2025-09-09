package com.genomic.common;

import lombok.Getter;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * ProtocolRequest - Parses and validates genomic protocol request messages
 * Handles the parsing of raw protocol messages into structured request objects
 * Supports all CRUD operations with proper validation and error handling
 */
@Getter
public class ProtocolRequest {
    // Getters
    private String command;
    private String patientId;
    private JSONObject metadata;
    private String fastaContent;

    /**
     * Constructs a ProtocolRequest by parsing a raw protocol message
     * @param rawRequest the raw protocol message string to parse
     * @throws ProtocolException if the request is malformed or invalid
     */
    public ProtocolRequest(String rawRequest) throws ProtocolException {
        parseRequest(rawRequest);
    }

    /**
     * Parses the raw request string into structured components
     * Validates command format, required parameters, and data integrity
     * @param rawRequest the raw protocol message to parse
     * @throws ProtocolException if parsing fails or validation errors occur
     */
    private void parseRequest(String rawRequest) throws ProtocolException {
        try {
            System.out.println("Parsing raw request: " + rawRequest);

            // Remove any trailing END markers or whitespace
            String cleanRequest = rawRequest.replace("\nEND\n", "").replace("END", "").trim();
            System.out.println("Cleaned request: " + cleanRequest);

            String[] parts = cleanRequest.split("\\" + ProtocolConstants.DELIMITER, 3);

            if (parts.length < 1) {
                throw new ProtocolException("Empty request", ProtocolConstants.ERR_INVALID_FORMAT);
            }

            this.command = parts[0].trim();
            System.out.println("Command: " + command);

            switch (command) {
                case ProtocolConstants.CMD_CREATE_PATIENT:
                    if (parts.length < 3) {
                        throw new ProtocolException("CREATE_PATIENT requires metadata and FASTA",
                                ProtocolConstants.ERR_INVALID_FORMAT);
                    }
                    this.metadata = new JSONObject(parts[1]);
                    this.fastaContent = parts[2];
                    System.out.println("Metadata: " + metadata);
                    System.out.println("FASTA content length: " + fastaContent.length());
                    break;

                case ProtocolConstants.CMD_GET_PATIENT:
                    if (parts.length < 2) {
                        throw new ProtocolException("GET_PATIENT requires patient ID",
                                ProtocolConstants.ERR_INVALID_FORMAT);
                    }
                    this.patientId = parts[1].trim();
                    System.out.println("Patient ID: " + patientId);
                    break;

                case ProtocolConstants.CMD_UPDATE_PATIENT:
                    if (parts.length < 3) {
                        System.out.println("UPDATE_PATIENT ERROR - Expected at least 3 parts, got " + parts.length);
                        for (int i = 0; i < parts.length; i++) {
                            System.out.println("  Part " + i + ": '" + parts[i] + "'");
                        }
                        throw new ProtocolException("UPDATE_PATIENT requires patient ID and metadata",
                                ProtocolConstants.ERR_INVALID_FORMAT);
                    }

                    this.patientId = parts[1].trim();
                    this.metadata = new JSONObject(parts[2]);

                    // FASTA is optional - if provided, it will be in part 3
                    this.fastaContent = parts.length > 3 ? parts[3] : null;

                    System.out.println("UPDATE - Patient ID: " + patientId);
                    System.out.println("UPDATE - Metadata: " + metadata.toString());
                    System.out.println("UPDATE - FASTA provided: " + (fastaContent != null));
                    if (fastaContent != null) {
                        System.out.println("UPDATE - FASTA length: " + fastaContent.length());
                    }
                    break;

                case ProtocolConstants.CMD_DELETE_PATIENT:
                    if (parts.length < 2) {
                        throw new ProtocolException("DELETE_PATIENT requires patient ID",
                                ProtocolConstants.ERR_INVALID_FORMAT);
                    }
                    this.patientId = parts[1].trim();
                    break;

                case ProtocolConstants.CMD_GET_PATIENT_COUNT:
                    this.patientId = parts[0].trim();
                    break;

                default:
                    throw new ProtocolException("Unknown command: " + command,
                            ProtocolConstants.ERR_INVALID_FORMAT);
            }

        } catch (Exception e) {
            System.err.println("Parse error: " + e.getMessage());
            throw new ProtocolException("Failed to parse request: " + e.getMessage(),
                    ProtocolConstants.ERR_INVALID_FORMAT);
        }
    }
}