package com.genomic.common;

import org.json.JSONObject;

/**
 * @param status Getters
 */
public record ProtocolResponse(String status, JSONObject data, String errorCode, String errorMessage) {

    public static ProtocolResponse success(JSONObject data) {
        return new ProtocolResponse(ProtocolConstants.RESP_SUCCESS, data, null, null);
    }

    public static ProtocolResponse success(String message) {
        JSONObject data = new JSONObject();
        data.put("message", message);
        return success(data);
    }

    public static ProtocolResponse error(String errorCode, String errorMessage) {
        return new ProtocolResponse(ProtocolConstants.RESP_ERROR, null, errorCode, errorMessage);
    }

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

    public boolean isSuccess() {
        return ProtocolConstants.RESP_SUCCESS.equals(status);
    }
}