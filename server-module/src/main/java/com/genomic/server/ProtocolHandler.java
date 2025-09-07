package com.genomic.server;

import com.genomic.common.*;
import org.json.JSONObject;
import com.genomic.server.service.PatientService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * ProtocolHandler - Handles client requests and processes genomic protocol commands
 * Each instance handles a single client connection in a separate thread
 */
public record ProtocolHandler(Socket clientSocket, PatientService patientService) {

    /**
     * Processes a client request from start to finish
     * Handles reading, parsing, processing, and response sending
     */
    public void handleRequest() {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {

            System.out.println("[" + Thread.currentThread().getName() + "] Reading request from client...");

            // Read the complete request in one go
            String rawRequest = dis.readUTF();
            System.out.println("[" + Thread.currentThread().getName() + "] Complete request received: " + rawRequest);

            // Remove the END marker if present
            if (rawRequest.endsWith("\nEND\n") || rawRequest.endsWith("END\n")) {
                rawRequest = rawRequest.substring(0, rawRequest.lastIndexOf("END")).trim();
            }

            System.out.println("[" + Thread.currentThread().getName() + "] Processing request: " + rawRequest);

            ProtocolRequest request = new ProtocolRequest(rawRequest);
            ProtocolResponse response = processRequest(request);

            // Send response
            String responseStr = response.toProtocolString();
            System.out.println("[" + Thread.currentThread().getName() + "] Sending response: " + responseStr);
            dos.writeUTF(responseStr);

        } catch (ProtocolException e) {
            try {
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                String errorResponse = e.toResponse().toProtocolString();
                System.out.println("[" + Thread.currentThread().getName() + "] Sending error response: " + errorResponse);
                dos.writeUTF(errorResponse);
            } catch (IOException ioException) {
                System.err.println("Failed to send error response: " + ioException.getMessage());
            }
        } catch (IOException e) {
            System.err.println("I/O error handling request: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            try {
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                ProtocolResponse error = ProtocolResponse.error(
                        ProtocolConstants.ERR_SERVER_ERROR, "Internal server error");
                dos.writeUTF(error.toProtocolString());
            } catch (IOException ioException) {
                System.err.println("Failed to send error response: " + ioException.getMessage());
            }
        }
    }

    /**
     * Processes the protocol request and returns appropriate response
     */
    private ProtocolResponse processRequest(ProtocolRequest request) {
        try {
            switch (request.getCommand()) {
                case ProtocolConstants.CMD_CREATE_PATIENT:
                    String patientId = patientService.createPatient(
                            request.getMetadata(), request.getFastaContent());
                    JSONObject createResponse = new JSONObject();
                    createResponse.put("patientId", patientId);
                    createResponse.put("message", "Patient created successfully");
                    return ProtocolResponse.success(createResponse);

                case ProtocolConstants.CMD_GET_PATIENT:
                    JSONObject patientData = patientService.getPatient(request.getPatientId());
                    return ProtocolResponse.success(patientData);

                case ProtocolConstants.CMD_UPDATE_PATIENT:
                    patientService.updatePatient(
                            request.getPatientId(), request.getMetadata(), request.getFastaContent());
                    return ProtocolResponse.success("Patient updated successfully");

                case ProtocolConstants.CMD_DELETE_PATIENT:
                    patientService.deletePatient(request.getPatientId());
                    return ProtocolResponse.success("Patient deleted successfully");

                default:
                    return ProtocolResponse.error(
                            ProtocolConstants.ERR_INVALID_FORMAT, "Unknown command: " + request.getCommand());
            }
        } catch (ProtocolException e) {
            return e.toResponse();
        } catch (Exception e) {
            return ProtocolResponse.error(
                    ProtocolConstants.ERR_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }
}