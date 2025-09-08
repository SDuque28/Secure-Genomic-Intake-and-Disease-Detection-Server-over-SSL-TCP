package com.genomic.client;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * EnhancedClient - Interactive command-line client for the Genomic Server
 * Provides a user-friendly interface for managing patient genomic data
 * Supports all CRUD operations and batch processing
 */
public class EnhancedClient {
    private final TCPClient tcpClient; // TCP client for server communication
    private final ProtocolClient protocolClient; // Protocol handler for message formatting
    private final Scanner scanner; // Scanner for user input
    private final String address = "192.168.193.250";

    /**
     * Constructor - Initializes the client components
     */
    public EnhancedClient() {
        this.tcpClient = new TCPClient(address, 2020);
        this.protocolClient = new ProtocolClient();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Main interactive mode - Entry point for user interaction
     * Displays menu and handles user choices in a loop
     */
    public void startInteractiveMode() {
        System.out.println("=== Genomic Client ===");
        System.out.println("Connected to server: " + address);

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    createPatientInteractive();
                    break;
                case "2":
                    getPatientInteractive();
                    break;
                case "3":
                    updatePatientInteractive();
                    break;
                case "4":
                    deletePatientInteractive();
                    break;
                case "5":
                    batchOperationsInteractive();
                    break;
                case "6":
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Displays the main menu options to the user
     */
    private void printMenu() {
        System.out.println("\n=== Menu ===");
        System.out.println("1. Create Patient");
        System.out.println("2. Get Patient Information");
        System.out.println("3. Update Patient");
        System.out.println("4. Delete Patient");
        System.out.println("5. Batch Operations");
        System.out.println("6. Exit");
        System.out.print("Choose an option: ");
    }

    /**
     * Interactive method to create a new patient
     * Collects metadata and FASTA sequence from user
     */
    private void createPatientInteractive() {
        try {
            System.out.println("\n=== Create New Patient ===");

            JSONObject metadata = readPatientMetadata();
            System.out.println("Enter FASTA sequence (type '>END' on a new line to finish):");
            String fastaContent = readMultilineInput();

            System.out.println("Sending request to server...");
            String response = protocolClient.sendCreatePatient(tcpClient, metadata, fastaContent);
            displayResponse(response);

        } catch (Exception e) {
            System.out.println("Error creating patient: " + e.getMessage());
        }
    }

    /**
     * Interactive method to create a new patient
     * Collects metadata and FASTA sequence from user
     */
    private void getPatientInteractive() {
        try {
            System.out.println("\n=== Get Patient Information ===");
            System.out.print("Enter Patient ID: ");
            String patientId = scanner.nextLine().trim();

            if (patientId.isEmpty()) {
                System.out.println("Patient ID cannot be empty.");
                return;
            }

            System.out.println("Fetching patient information...");
            String response = protocolClient.sendGetPatient(tcpClient, patientId);
            displayFormattedResponse(response);

        } catch (IOException e) {
            System.out.println("Error retrieving patient: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Interactive method to update an existing patient
     * Shows current data and allows partial updates
     */
    private void updatePatientInteractive() {
        try {
            System.out.println("\n=== Update Patient ===");
            System.out.print("Enter Patient ID to update: ");
            String patientId = scanner.nextLine().trim();

            if (patientId.isEmpty()) {
                System.out.println("Patient ID cannot be empty.");
                return;
            }

            // First get current patient data to show what we're updating
            System.out.println("Fetching current patient data...");
            String currentData = protocolClient.sendGetPatient(tcpClient, patientId);

            if (currentData.startsWith("ERROR")) {
                displayResponse(currentData);
                return;
            }

            System.out.println("\nCurrent patient data:");
            displayFormattedResponse(currentData);

            System.out.println("\nEnter new metadata (press Enter to keep current value):");
            JSONObject currentMetadata = extractMetadataFromResponse(currentData);
            JSONObject newMetadata = readPatientMetadataWithDefaults(currentMetadata);

            System.out.println("Update FASTA sequence? (y/N): ");
            String updateFasta = scanner.nextLine().trim().toLowerCase();
            String fastaContent = null;

            if (updateFasta.equals("y") || updateFasta.equals("yes")) {
                System.out.println("Enter new FASTA sequence (type '>END' on a new line to finish):");
                fastaContent = readMultilineInput();
            }

            System.out.println("Sending update request...");
            String response = protocolClient.sendUpdatePatient(tcpClient, patientId, newMetadata, fastaContent);
            displayResponse(response);

        } catch (IOException e) {
            System.out.println("Error updating patient: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Interactive method to delete a patient
     * Includes confirmation prompt for safety
     */
    private void deletePatientInteractive() {
        try {
            System.out.println("\n=== Delete Patient ===");
            System.out.print("Enter Patient ID to delete: ");
            String patientId = scanner.nextLine().trim();

            if (patientId.isEmpty()) {
                System.out.println("Patient ID cannot be empty.");
                return;
            }

            // Confirm deletion
            System.out.print("Are you sure you want to delete patient " + patientId + "? (yes/NO): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (!confirmation.equals("yes")) {
                System.out.println("Deletion cancelled.");
                return;
            }

            System.out.println("Deleting patient...");
            String response = protocolClient.sendDeletePatient(tcpClient, patientId);
            displayResponse(response);

        } catch (IOException e) {
            System.out.println("Error deleting patient: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Sub-menu for batch operations
     */
    private void batchOperationsInteractive() {
        System.out.println("\n=== Batch Operations ===");
        System.out.println("1. Create Multiple Test Patients");
        System.out.println("2. Back to Main Menu");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                createMultipleTestPatients();
                break;
            case "2":
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    /**
     * Creates multiple test patients for load testing or demonstration
     */
    private void createMultipleTestPatients() {
        try {
            System.out.print("How many test patients to create? ");
            int count = Integer.parseInt(scanner.nextLine().trim());

            if (count <= 0 || count > 100) {
                System.out.println("Please enter a number between 1 and 100.");
                return;
            }

            // Get the current highest patient number from existing test patients
            int baseNumber = protocolClient.sendGetAllPatients(tcpClient) + 1;

            System.out.println("Creating " + count + " test patients starting from number " + baseNumber + "...");

            int successfulCreations = 0;
            int failedCreations = 0;

            for (int i = 0; i < count; i++) {
                int patientNumber = baseNumber + i;

                JSONObject metadata = getJsonObject(patientNumber);

                String fastaContent = ">test_patient_" + patientNumber + "\n" +
                        "ACGTACGTGGCCTTAAACCGGTAGCTAGCTAGGCTAGCTAGCTAGCTA\n" +
                        "GCTAGCTAGCGATCGATCGTAAACGTACGTGGCCTTAAACCGGTAGC\n" +
                        "TAGCTAGGCTAGCTAGCTAGCTAGCTAGCTAGCGATCGATCGTAA";

                try {
                    String response = protocolClient.sendCreatePatient(tcpClient, metadata, fastaContent);

                    if (response.startsWith("SUCCESS")) {
                        System.out.println("Created patient " + patientNumber + " (" + (i + 1) + "/" + count + ")");
                        successfulCreations++;
                    } else {
                        System.out.println("Failed to create patient " + patientNumber + ": " + response);
                        failedCreations++;
                    }

                    // Small delay to avoid overwhelming the server
                    Thread.sleep(100);

                } catch (IOException e) {
                    System.out.println("Network error creating patient " + patientNumber + ": " + e.getMessage());
                    failedCreations++;
                }
            }

            System.out.println("Batch creation completed. Success: " + successfulCreations + ", Failed: " + failedCreations);

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (InterruptedException e) {
            System.out.println("Batch operation interrupted.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Reads patient metadata from user input
     * @return JSONObject containing patient metadata
     */
    private static JSONObject getJsonObject(int patientNumber) {
        JSONObject metadata = new JSONObject();
        metadata.put("fullName", "Test Patient " + patientNumber);
        metadata.put("documentId", "TEST" + String.format("%04d", patientNumber));
        metadata.put("age", 25 + (patientNumber % 45));
        metadata.put("sex", patientNumber % 2 == 0 ? "M" : "F");
        metadata.put("email", "test.patient" + patientNumber + "@example.com");
        metadata.put("clinicalNotes", "Automated test patient #" + patientNumber);
        return metadata;
    }

    /**
     * Finds the highest test patient number in the existing database
     * by attempting to retrieve patients and checking their document IDs
     */
    private int findHighestTestPatientNumber() {
        int highestNumber = 0;
        int consecutiveMissing = 0;
        int maxConsecutiveMissing = 2; // Stop after 5 consecutive missing patients

        for (int i = 1; i <= 1000; i++) {
            String patientId = "PAT" + String.format("%06d", i);

            try {
                String response = protocolClient.sendGetPatient(tcpClient, patientId);

                if (response.startsWith("SUCCESS")) {
                    // Patient exists and is active
                    highestNumber = i;
                    consecutiveMissing = 0; // Reset counter
                    System.out.println("Found active patient: " + patientId);
                } else if (response.startsWith("ERROR")) {
                    // Patient not found or inactive
                    consecutiveMissing++;
                    System.out.println("Patient not found: " + patientId + " (" + consecutiveMissing + " consecutive missing)");

                    // If we've found enough consecutive missing patients, stop searching
                    if (consecutiveMissing >= maxConsecutiveMissing && highestNumber > 0) {
                        System.out.println("Stopping search after " + maxConsecutiveMissing + " consecutive missing patients");
                        break;
                    }
                }

            } catch (IOException e) {
                System.out.println("Network error checking patient " + patientId + ": " + e.getMessage());
                break;
            }

            // Small delay to avoid overwhelming server
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return highestNumber;
    }

    /**
     * We need to see the object parameters for the extraction
     * @return JSONObject to retrieve the metadata
     */
    private JSONObject readPatientMetadata() {
        JSONObject metadata = new JSONObject();

        System.out.print("Full Name: ");
        metadata.put("fullName", scanner.nextLine().trim());

        System.out.print("Document ID: ");
        metadata.put("documentId", scanner.nextLine().trim());

        System.out.print("Age: ");
        metadata.put("age", Integer.parseInt(scanner.nextLine().trim()));

        System.out.print("Sex (M/F): ");
        metadata.put("sex", scanner.nextLine().trim().toUpperCase());

        System.out.print("Email: ");
        metadata.put("email", scanner.nextLine().trim());

        System.out.print("Clinical Notes: ");
        metadata.put("clinicalNotes", scanner.nextLine().trim());

        return metadata;
    }

    /**
     * We need to see the object parameters for the extraction
     * @return JSONObject to retrieve the metadata with defaults
     */
    private JSONObject readPatientMetadataWithDefaults(JSONObject currentMetadata) {
        JSONObject metadata = new JSONObject();

        System.out.print("Full Name [" + currentMetadata.optString("fullName") + "]: ");
        String fullName = scanner.nextLine().trim();
        metadata.put("fullName", fullName.isEmpty() ? currentMetadata.getString("fullName") : fullName);

        System.out.print("Document ID [" + currentMetadata.optString("documentId") + "]: ");
        String documentId = scanner.nextLine().trim();
        metadata.put("documentId", documentId.isEmpty() ? currentMetadata.getString("documentId") : documentId);

        System.out.print("Age [" + currentMetadata.optInt("age") + "]: ");
        String ageInput = scanner.nextLine().trim();
        metadata.put("age", ageInput.isEmpty() ? currentMetadata.getInt("age") : Integer.parseInt(ageInput));

        System.out.print("Sex (M/F) [" + currentMetadata.optString("sex") + "]: ");
        String sex = scanner.nextLine().trim();
        metadata.put("sex", sex.isEmpty() ? currentMetadata.getString("sex") : sex.toUpperCase());

        System.out.print("Email [" + currentMetadata.optString("email") + "]: ");
        String email = scanner.nextLine().trim();
        metadata.put("email", email.isEmpty() ? currentMetadata.getString("email") : email);

        System.out.print("Clinical Notes [" + currentMetadata.optString("clinicalNotes") + "]: ");
        String clinicalNotes = scanner.nextLine().trim();
        metadata.put("clinicalNotes", clinicalNotes.isEmpty() ? currentMetadata.getString("clinicalNotes") : clinicalNotes);

        return metadata;
    }

    /**
     * Reads multi-line FASTA input until >END marker
     * @return concatenated FASTA content as String
     */
    private String readMultilineInput() {
        StringBuilder content = new StringBuilder();
        String line;

        System.out.println("Enter FASTA content (type '>END' on a new line to finish):");
        while (true) {
            line = scanner.nextLine();
            if (line.trim().equals(">END")) {
                break;
            }
            content.append(line).append("\n");
        }

        return content.toString().trim();
    }

    /**
     * Displays server response in user-friendly format
     * @param response raw response string from server
     */
    private void displayResponse(String response) {
        if (response.startsWith("SUCCESS|")) {
            System.out.println("✅ SUCCESS: " + response.substring(8));
        } else if (response.startsWith("ERROR|")) {
            String errorJson = response.substring(6);
            try {
                JSONObject errorObj = new JSONObject(errorJson);
                System.out.println("❌ ERROR: " + errorObj.optString("message", "Unknown error"));
                System.out.println("   Code: " + errorObj.optString("code", "UNKNOWN"));
            } catch (Exception e) {
                System.out.println("❌ ERROR: " + errorJson);
            }
        } else {
            System.out.println("Response: " + response);
        }
    }

    /**
     * Displays formatted patient information
     * @param response raw response string from server
     */
    private void displayFormattedResponse(String response) {
        if (response.startsWith("SUCCESS|")) {
            String jsonPart = response.substring(8);
            try {
                JSONObject json = new JSONObject(jsonPart);
                System.out.println("\n=== Patient Information ===");
                System.out.println("Patient ID: " + json.optString("patientId", "N/A"));
                System.out.println("Full Name: " + json.optString("fullName", "N/A"));
                System.out.println("Document ID: " + json.optString("documentId", "N/A"));
                System.out.println("Age: " + json.optInt("age", 0));
                System.out.println("Sex: " + json.optString("sex", "N/A"));
                System.out.println("Email: " + json.optString("email", "N/A"));
                System.out.println("Registration Date: " + json.optLong("registrationDate", 0));
                System.out.println("Clinical Notes: " + json.optString("clinicalNotes", "N/A"));
                System.out.println("FASTA Checksum: " + json.optString("checksumFasta", "N/A"));
                System.out.println("File Size: " + json.optLong("fileSizeBytes", 0) + " bytes");
                System.out.println("FASTA Filename: " + json.optString("fastaFilename", "N/A"));
                System.out.println("Active: " + json.optBoolean("active", true));
            } catch (Exception e) {
                System.out.println("✅ SUCCESS: " + jsonPart);
            }
        } else {
            displayResponse(response);
        }
    }

    /**
     * Extracts metadata from server response
     * @param response raw response string from server
     * @return JSONObject containing patient metadata
     */
    private JSONObject extractMetadataFromResponse(String response) {
        if (response.startsWith("SUCCESS|")) {
            String jsonPart = response.substring(8);
            try {
                return new JSONObject(jsonPart);
            } catch (Exception e) {
                // If parsing fails, return empty metadata
            }
        }
        return new JSONObject();
    }

    /**
     * Main method - Entry point for the EnhancedClient
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        EnhancedClient client = new EnhancedClient();
        client.startInteractiveMode();
    }
}