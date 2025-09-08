package com.genomic.client;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * ProtocolClient - Handles the communication protocol between client and genomic server
 * Provides methods for all CRUD operations using a custom text-based protocol
 * Formats requests and parses responses according to the genomic protocol specification
 */
public class ProtocolClient {

    /**
     * Sends a CREATE_PATIENT request to the server
     * Creates a new patient with metadata and genomic FASTA data
     *
     * @param tcpClient TCP client instance for server communication
     * @param metadata JSON object containing patient demographic and clinical information
     * @param fastaContent genomic sequence data in FASTA format
     * @return server response as a raw string
     * @throws IOException if network communication fails
     */
    public String sendCreatePatient(TCPClient tcpClient, JSONObject metadata, String fastaContent) throws IOException {
        tcpClient.connect();

        DataOutputStream dos = tcpClient.getDataOutputStream();
        DataInputStream dis = tcpClient.getDataInputStream();

        // Format: CREATE_PATIENT|{metadata_json}|{fasta_content}
        String request = "CREATE_PATIENT|" +
                metadata.toString() + "|" +
                fastaContent;

        System.out.println("Sending CREATE request length: " + request.length());
        dos.writeUTF(request);

        String response = dis.readUTF();
        System.out.println("Received CREATE response: " + response);

        tcpClient.closeConnection();
        return response;
    }

    /**
     * Sends a GET_PATIENT request to the server
     * Retrieves patient information by patient ID
     *
     * @param tcpClient TCP client instance for server communication
     * @param patientId unique identifier of the patient to retrieve
     * @return server response containing patient data
     * @throws IOException if network communication fails
     */
    public String sendGetPatient(TCPClient tcpClient, String patientId) throws IOException {
        tcpClient.connect();

        DataOutputStream dos = tcpClient.getDataOutputStream();
        DataInputStream dis = tcpClient.getDataInputStream();

        String request = "GET_PATIENT|" + patientId;

        System.out.println("Sending GET request: " + request);
        dos.writeUTF(request);

        String response = dis.readUTF();
        System.out.println("Received GET response: " + response);

        tcpClient.closeConnection();
        return response;
    }

    public int sendGetAllPatients(TCPClient tcpClient) throws IOException {
        tcpClient.connect();

        DataOutputStream dos = tcpClient.getDataOutputStream();
        DataInputStream dis = tcpClient.getDataInputStream();

        String request = "GET_PATIENT_COUNT";

        System.out.println("Sending GET ALL request: " + request);
        dos.writeUTF(request);

        String response = dis.readUTF();
        System.out.println("Received GET ALl response: " + response);

        String[] parts = response.split(":");
        String numberStr = parts[1].trim();

        tcpClient.closeConnection();

        return Integer.parseInt(numberStr);
    }

    /**
     * Sends an UPDATE_PATIENT request to the server
     * Updates existing patient information and optionally updates genomic data
     * FASTA content is optional - only included if provided
     *
     * @param tcpClient TCP client instance for server communication
     * @param patientId unique identifier of the patient to update
     * @param metadata JSON object containing updated patient information
     * @param fastaContent optional updated genomic sequence data (can be null)
     * @return server response indicating update status
     * @throws IOException if network communication fails
     */
    public String sendUpdatePatient(TCPClient tcpClient, String patientId,
                                    JSONObject metadata, String fastaContent) throws IOException {
        tcpClient.connect();

        DataOutputStream dos = tcpClient.getDataOutputStream();
        DataInputStream dis = tcpClient.getDataInputStream();

        // Build request with simple pipe delimiter
        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append("UPDATE_PATIENT|")
                .append(patientId).append("|")
                .append(metadata.toString());

        // Add FASTA only if provided
        if (fastaContent != null && !fastaContent.trim().isEmpty()) {
            requestBuilder.append("|").append(fastaContent);
        }

        String request = requestBuilder.toString();
        System.out.println("Sending UPDATE request length: " + request.length());
        dos.writeUTF(request);

        String response = dis.readUTF();
        System.out.println("Received UPDATE response: " + response);

        tcpClient.closeConnection();
        return response;
    }

    /**
     * Sends a DELETE_PATIENT request to the server
     * Marks a patient as inactive (logical deletion)
     *
     * @param tcpClient TCP client instance for server communication
     * @param patientId unique identifier of the patient to delete
     * @return server response indicating deletion status
     * @throws IOException if network communication fails
     */
    public String sendDeletePatient(TCPClient tcpClient, String patientId) throws IOException {
        tcpClient.connect();

        DataOutputStream dos = tcpClient.getDataOutputStream();
        DataInputStream dis = tcpClient.getDataInputStream();

        String request = "DELETE_PATIENT|" + patientId;

        System.out.println("Sending DELETE request: " + request);
        dos.writeUTF(request);

        String response = dis.readUTF();
        System.out.println("Received DELETE response: " + response);

        tcpClient.closeConnection();
        return response;
    }
}