package com.genomic.client;

import lombok.Getter;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TCPClient - Handles secure SSL/TLS communication with the genomic server
 * Manages connection establishment, SSL handshake, and network operations
 * Supports both single messages and concurrent connection testing
 */
public class TCPClient {
    @Getter
    private DataInputStream dataInputStream;
    @Getter
    private DataOutputStream dataOutputStream;
    private final String serverAddress;
    private final int serverPort;
    private Socket clientSocket;
    private final SSLContext sslContext;

    /**
     * Constructor - Initializes TCP client with server connection details
     * @param serverAddress IP address or hostname of the genomic server
     * @param serverPort port number of the genomic server
     */
    public TCPClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.sslContext = createSSLContext();
    }

    /**
     * Creates and configures SSL context for secure communication
     * Loads certificate from configuration and sets up trust management
     *
     * @return Configured SSLContext instance, or null if initialization fails
     */
    private SSLContext createSSLContext() {
        try {
            Properties p = new Properties();
            try (InputStream input = TCPClient.class.getClassLoader().getResourceAsStream("configuration.properties")) {
                p.load(input);
            } catch (IOException ex) {
                Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }

            String certificateRoute = p.getProperty("SSL_CERTIFICATE_ROUTE");
            String certificatePassword = p.getProperty("SSL_PASSWORD");

            // Load the keystore (which will also be our truststore for self-signed cert)
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream keyStoreStream = TCPClient.class.getClassLoader().getResourceAsStream(certificateRoute)) {
                if (keyStoreStream == null) {
                    throw new IOException("Certificate file not found: " + certificateRoute);
                }
                keyStore.load(keyStoreStream, certificatePassword.toCharArray());
            }

            // Setup trust manager factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            // Create SSL context
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagerFactory.getTrustManagers(), null);

            return context;

        } catch (Exception e) {
            System.err.println("Error creating SSL context: " + e.getMessage());
            return null;
        }
    }

    /**
     * Establishes secure SSL connection to the server
     * Performs SSL handshake and initializes data streams
     * @throws IOException if connection fails or SSL handshake is unsuccessful
     */
    public void connect() throws IOException {
        if (sslContext == null) {
            throw new IOException("SSL context not initialized");
        }

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(serverAddress, serverPort);

        // Configure SSL socket
        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
        sslSocket.startHandshake();

        System.out.println("Connected to server: " + this.serverAddress + ":" + this.serverPort);
        System.out.println("SSL handshake completed successfully");

        DataInputStream dataInputStream = new DataInputStream(sslSocket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(sslSocket.getOutputStream());

        // Store in instance variables
        this.clientSocket = sslSocket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    /**
     * Sends a simple test message to the server (legacy method)
     * Uses format: name:lastName
     * @param name first name to send
     * @param lastName last name to send
     * @return server response as string
     * @throws IOException if communication fails
     */
    public String sendMessage(String name, String lastName) throws IOException {
        this.connect();
        String message = name + ":" + lastName;
        System.out.println("Sending message: " + message);
        this.dataOutputStream.writeUTF(message);
        String response = this.dataInputStream.readUTF();
        this.closeConnection();
        return response;
    }

    /**
     * Tests multiple simultaneous connections to the server
     * Useful for load testing and concurrency verification
     * @param numConnections number of concurrent connections to test
     * @param baseName base name for test client identification
     */
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

    /**
     * Safely closes all network connections and streams
     * Ensures proper resource cleanup to prevent memory leaks
     */
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