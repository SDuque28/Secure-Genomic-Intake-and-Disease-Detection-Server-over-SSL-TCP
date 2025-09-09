package com.genomic.server;

import com.genomic.server.service.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main - Entry point for the Genomic Server application
 * Initializes SSL context, loads configuration, and starts the TCP server
 */
public class Main {

    /**
     * Main entry point for the genomic server application
     *
     * @param args command line arguments (not currently used)
     */
    public static void main(String[] args) {
        Properties p = new Properties();

        //Loads configuration properties from file
        //@throws IOException if configuration file cannot be loaded
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("configuration.properties")) {
            p.load(input);
            System.out.println("Properties loaded successfully!");
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        // Creates and configures SSL context for secure communication
        String certificateRoute = p.getProperty("SSL_CERTIFICATE_ROUTE");
        String certificatePassword = p.getProperty("SSL_PASSWORD");
        System.out.println("Certificate route: " + certificateRoute + " and password: " + certificatePassword);

        try {
            // Load the keystore from classpath
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream keyStoreStream = Main.class.getClassLoader().getResourceAsStream(certificateRoute)) {
                keyStore.load(keyStoreStream, certificatePassword.toCharArray());
            }

            // Setup key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, certificatePassword.toCharArray());

            // Setup trust manager factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            System.out.println("SSL Context created successfully with protocol: " + sslContext.getProtocol());

            // Initialize services
            DiseaseService diseaseService = new DiseaseService();
            PatientService patientService = new PatientService(diseaseService);
            System.out.println("Services initialized successfully");

            // Pass the SSLContext to your TCPServer
            TCPServer server = new TCPServer(2020, sslContext);
            server.start();

        } catch (Exception e) {
            System.err.println("Error setting up SSL: " + e.getMessage());
        }
    }
}