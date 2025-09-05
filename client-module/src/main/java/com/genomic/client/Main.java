package com.genomic.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Properties p = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("configuration.properties")) {
            p.load(input);
            System.out.println("Properties loaded successfully!");
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        String certificateRoute = p.getProperty("SSL_CERTIFICATE_ROUTE");
        String certificatePassword = p.getProperty("SSL_PASSWORD");

        // Get the certificate file from classpath and decode the URL
        URL certUrl = Main.class.getClassLoader().getResource(certificateRoute);
        if (certUrl != null) {
            try {
                String absoluteCertPath = URLDecoder.decode(certUrl.getPath(), StandardCharsets.UTF_8);

                // For Windows paths, remove leading slash if present
                if (absoluteCertPath.startsWith("/") && absoluteCertPath.contains(":")) {
                    absoluteCertPath = absoluteCertPath.substring(1);
                }

                System.setProperty("javax.net.ssl.keyStore", absoluteCertPath);
                System.setProperty("javax.net.ssl.keyStorePassword", certificatePassword);
                System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
                System.setProperty("javax.net.ssl.trustStore", absoluteCertPath);
                System.setProperty("javax.net.ssl.trustStorePassword", certificatePassword);
                System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");

                System.out.println("SSL properties set successfully");

            } catch (Exception e) {
                System.err.println("Error processing certificate path: " + e.getMessage());
                return;
            }
        } else {
            System.err.println("Certificate file not found: " + certificateRoute);
            return;
        }

        TCPClient client = new TCPClient("169.254.7.149", 2020);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Client Menu ===");
            System.out.println("1. Send single message");
            System.out.println("2. Test multiple simultaneous connections");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        System.out.print("Enter name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter last name: ");
                        String lastName = scanner.nextLine();

                        try {
                            String response = client.sendMessage(name, lastName);
                            System.out.println("Server response: " + response);
                        } catch (IOException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        break;

                    case 2:
                        System.out.print("Number of simultaneous connections: ");
                        int numConnections = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Base name for clients: ");
                        String baseName = scanner.nextLine();

                        client.testMultipleConnections(numConnections, baseName);
                        break;

                    case 3:
                        System.out.println("Exiting...");
                        scanner.close();
                        return;

                    default:
                        System.out.println("Invalid option. Please choose 1, 2, or 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}