package com.genomic.server;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main2 {
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
        System.out.println("Certificate route: " + certificateRoute + " and password: " + certificatePassword);

        try {
            // Load the keystore from classpath
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream keyStoreStream = Main.class.getClassLoader().getResourceAsStream(certificateRoute)) {
                if (keyStoreStream == null) {
                    throw new IOException("Certificate file not found in classpath: " + certificateRoute);
                }
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

            // Pass the SSLContext to your TCPServer
            TCPServer2 server = new TCPServer2(2020, sslContext);
            server.start();

        } catch (Exception e) {
            System.err.println("Error setting up SSL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}