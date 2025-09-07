package com.genomic.client;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ProtocolClient {

    public String sendCreatePatient(TCPClient tcpClient, JSONObject metadata, String fastaContent) throws IOException {
        tcpClient.connect();

        DataOutputStream dos = tcpClient.getDataOutputStream();
        DataInputStream dis = tcpClient.getDataInputStream();

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