package com.genomic.common;

import org.json.JSONObject;

/**
 * ProtocolResponse - Represents a standardized response in the genomic protocol
 * Immutable record that encapsulates success/error status, data payload, and error information
 * Used for consistent response formatting between client and server
 * @param status response status (SUCCESS or ERROR)
 * @param data JSON data payload for successful responses
 * @param errorCode machine-readable error code for error responses
 * @param errorMessage human-readable error description for error responses
 */
public record ProtocolResponse(String status, JSONObject data, String errorCode, String errorMessage) {

    /**
     * Creates a successful response with data payload
     * @param data JSON object containing response data
     * @return ProtocolResponse with SUCCESS status and provided data
     */
    public static ProtocolResponse success(JSONObject data) {
        return new ProtocolResponse(ProtocolConstants.RESP_SUCCESS, data, null, null);
    }

    /**
     * Creates a successful response with a simple message
     * @param message human-readable success message
     * @return ProtocolResponse with SUCCESS status and message payload
     */
    public static ProtocolResponse success(String message) {
        JSONObject data = new JSONObject();
        data.put("message", message);
        return success(data);
    }

    /**
     * Creates an error response from a ProtocolException
     * @param errorMessage the ProtocolException to convert
     * @param errorCode is the code for the error
     * @return ProtocolResponse with error information from the exception
     */
    public static ProtocolResponse error(String errorCode, String errorMessage) {
        return new ProtocolResponse(ProtocolConstants.RESP_ERROR, null, errorCode, errorMessage);
    }

    /**
     * Converts the response to protocol string format for transmission
     * Format: STATUS|{json_data}
     * For success: SUCCESS|{data_json}
     * For error: ERROR|{"code":"ERROR_CODE","message":"Error description"}
     * @return formatted protocol string ready for transmission
     */
    public String toProtocolString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ProtocolConstants.RESP_SUCCESS.equals(status) ?
                ProtocolConstants.RESP_SUCCESS : ProtocolConstants.RESP_ERROR);
        sb.append(ProtocolConstants.DELIMITER);

        if (ProtocolConstants.RESP_SUCCESS.equals(status)) {
            sb.append(data != null ? data.toString() : "{}");
        } else {
            JSONObject errorObj = new JSONObject();
            errorObj.put("code", errorCode);
            errorObj.put("message", errorMessage);
            sb.append(errorObj);
        }

        return sb.toString();
    }

    /**
     * Checks if the response represents a successful operation
     *
     * @return true if status is SUCCESS, false otherwise
     */
    public boolean isSuccess() {
        return ProtocolConstants.RESP_SUCCESS.equals(status);
    }
}