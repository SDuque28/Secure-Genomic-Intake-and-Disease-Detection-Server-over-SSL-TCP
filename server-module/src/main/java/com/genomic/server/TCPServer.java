package com.genomic.server;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLContext;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer2 {
    private int serverPort;
    private SSLContext sslContext;

    // Constructor with SSLContext
    public TCPServer2(int serverPort, SSLContext sslContext){
        this.serverPort = serverPort;
        this.sslContext = sslContext;
    }

    // Keep original constructor for backward compatibility
    public TCPServer2(int serverPort){
        this.serverPort = serverPort;
        this.sslContext = null;
    }

    public void start(){
        try{
            SSLServerSocketFactory sslSocketFactory;

            if (sslContext != null) {
                // Use the provided SSLContext
                sslSocketFactory = sslContext.getServerSocketFactory();
            } else {
                // Fallback to default (may not work with certificates)
                sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            }

            SSLServerSocket serverSocket = (SSLServerSocket) sslSocketFactory.createServerSocket(serverPort);

            // Optional: Configure SSL parameters
            // serverSocket.setEnabledCipherSuites(sslSocketFactory.getSupportedCipherSuites());
            // serverSocket.setNeedClientAuth(false);

            System.out.println("SSL Server started on port: " + serverPort);
            System.out.println("Enabled protocols: " + java.util.Arrays.toString(serverSocket.getEnabledProtocols()));
            System.out.println("Enabled ciphers: " + java.util.Arrays.toString(serverSocket.getEnabledCipherSuites()));

            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                String message = dis.readUTF();
                String[] parts = message.split(":");
                System.out.println("Received message: " + message);

                String response = "Name "+parts[0]+" Last Name "+parts[1];
                out.writeUTF(response);

                clientSocket.close();
                System.out.println("Client disconnected");
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}