package com.genomic.client;

import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPClient {
    private final String serverAddress;
    private final int serverPort;
    private Socket clientSocket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public TCPClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void connect() throws IOException {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        Socket clientSocket = sslSocketFactory.createSocket(serverAddress, serverPort);
        System.out.println("Connected to server: " + this.serverAddress + ":" + this.serverPort);
        DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

        // Store in instance variables if needed for other methods
        this.clientSocket = clientSocket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    public String sendMessage(String name, String lastName) throws IOException {
        this.connect();
        String message = name + ":" + lastName;
        System.out.println("Sending message: " + message);
        this.dataOutputStream.writeUTF(message);
        String response = this.dataInputStream.readUTF();
        this.closeConnection();
        return response;
    }

    // Method to test multiple simultaneous connections
    public void testMultipleConnections(int numConnections, String baseName) {
        ExecutorService executor = Executors.newFixedThreadPool(numConnections);
        CountDownLatch latch = new CountDownLatch(numConnections);

        System.out.println("Testing " + numConnections + " simultaneous connections...");

        for (int i = 0; i < numConnections; i++) {
            final int clientId = i + 1;
            executor.execute(() -> {
                try {
                    // Create a new client instance for each connection
                    TCPClient client = new TCPClient(serverAddress, serverPort);
                    String response = client.sendMessage(baseName + clientId, "Test" + clientId);
                    System.out.println("Client " + clientId + " received: " + response);
                } catch (IOException e) {
                    System.out.println("Client " + clientId + " error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(); // Wait for all threads to complete
            System.out.println("All " + numConnections + " connections completed successfully!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Test interrupted");
        } finally {
            executor.shutdown();
        }
    }

    public void closeConnection() {
        try {
            if (this.dataInputStream != null) this.dataInputStream.close();
            if (this.dataOutputStream != null) this.dataOutputStream.close();
            if (this.clientSocket != null) this.clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}