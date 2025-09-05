package com.genomic.server;

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

public record TCPServer(int serverPort, SSLContext sslContext) {

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

    // Inner class to handle each client connection in a separate thread
        private record ClientHandler(Socket clientSocket) implements Runnable {

        @Override
            public void run() {
                try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                     DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

                    String message = dis.readUTF();
                    String[] parts = message.split(":");
                    System.out.println("[" + Thread.currentThread().getName() + "] Received from " +
                            clientSocket.getRemoteSocketAddress() + ": " + message);

                    // Simulate some processing time to demonstrate concurrency
                    try {
                        Thread.sleep(2000); // 2 seconds processing time
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    String response = "Name " + parts[0] + " Last Name " + parts[1];
                    out.writeUTF(response);

                    System.out.println("[" + Thread.currentThread().getName() + "] Response sent to " +
                            clientSocket.getRemoteSocketAddress());

                } catch (IOException e) {
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