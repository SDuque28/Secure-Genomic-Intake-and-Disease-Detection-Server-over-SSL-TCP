package com.genomic.server;

import com.genomic.server.service.*;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLContext;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCPServer - Main server class that handles SSL/TLS connections and client requests
 * Manages thread pool, SSL configuration, and client connection handling
 */
public record TCPServer(int serverPort, SSLContext sslContext) {

    /**
     * Starts the SSL/TCP server and begins accepting client connections
     * Initializes thread pool, SSL socket, and enters main accept loop
     */
    public void start() {
        // Create a thread pool to handle multiple clients
        ExecutorService threadPool = Executors.newCachedThreadPool();
        try {
            SSLServerSocket serverSocket = getSslServerSocket();

            System.out.println("SSL Server started on port: " + serverPort);
            System.out.println("Enabled protocols: " + Arrays.toString(serverSocket.getEnabledProtocols()));
            System.out.println("Ready to handle multiple simultaneous connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    /**
     * Configures SSL server socket protocols and cipher suites
     */
    private SSLServerSocket getSslServerSocket() throws IOException {
        SSLServerSocketFactory sslSocketFactory;

        if (sslContext != null) {
            // Use the provided SSLContext
            sslSocketFactory = sslContext.getServerSocketFactory();
        } else {
            // Fallback to default (may not work with certificates)
            sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        }

        return (SSLServerSocket) sslSocketFactory.createServerSocket(serverPort);
    }

    /**
     * ClientHandler - Handles individual client connections in separate threads
     */
    private record ClientHandler(Socket clientSocket) implements Runnable {
        @Override
        public void run() {
            try {
                // Initialize services (you'll need to create PatientService)
                DiseaseService diseaseService = new DiseaseService();
                PatientService patientService = new PatientService(diseaseService); // You'll implement this later

                ProtocolHandler protocolHandler = new ProtocolHandler(clientSocket, patientService);
                protocolHandler.handleRequest();

            } catch (Exception e) {
                System.out.println("Error handling client " + clientSocket.getRemoteSocketAddress() + ": " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("[" + Thread.currentThread().getName() + "] Client disconnected: " +
                            clientSocket.getRemoteSocketAddress());
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}