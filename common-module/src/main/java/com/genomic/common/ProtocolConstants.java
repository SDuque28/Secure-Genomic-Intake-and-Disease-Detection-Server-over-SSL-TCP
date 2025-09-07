package com.genomic.common;

public class ProtocolConstants {
    public static final String DELIMITER = "|";

    // Commands
    public static final String CMD_CREATE_PATIENT = "CREATE_PATIENT";
    public static final String CMD_GET_PATIENT = "GET_PATIENT";
    public static final String CMD_UPDATE_PATIENT = "UPDATE_PATIENT";
    public static final String CMD_DELETE_PATIENT = "DELETE_PATIENT";

    // Responses
    public static final String RESP_SUCCESS = "SUCCESS";
    public static final String RESP_ERROR = "ERROR";

    // Error codes
    public static final String ERR_INVALID_FORMAT = "INVALID_FORMAT";
    public static final String ERR_PATIENT_NOT_FOUND = "PATIENT_NOT_FOUND";
    public static final String ERR_DUPLICATE_DOCUMENT = "DUPLICATE_DOCUMENT";
    public static final String ERR_INVALID_FASTA = "INVALID_FASTA";
    public static final String ERR_SERVER_ERROR = "SERVER_ERROR";
}
