package com.genomic.server.service;

import com.genomic.common.model.Disease;
import com.genomic.common.model.DiseaseMatchResult;
import com.genomic.common.model.Patient;
import com.genomic.common.ProtocolException;
import com.genomic.common.ProtocolConstants;
import com.genomic.common.util.FastaValidator;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PatientService - Manages patient records and genomic data operations
 * Handles patient CRUD operations, FASTA file storage, and disease detection
 */
public class PatientService {
    private final Map<String, Patient> patients = new ConcurrentHashMap<>();
    private final AtomicInteger patientCounter = new AtomicInteger(1);
    private final Path patientsDirectory;
    private final String patientCsvFile;
    private DiseaseService diseaseService;

    /**
     * Initializes the PatientService with disease service dependency
     * @param diseaseService the disease service for genomic matching
     * @throws ProtocolException if initialization fails
     */
    public PatientService(DiseaseService diseaseService) throws ProtocolException {
        this.diseaseService = diseaseService;
        try {
            // Create data directories
            Path dataDirectory = Paths.get("server-module/src/main/resources/data");
            patientsDirectory = dataDirectory.resolve("patients");
            Path reportsDirectory = dataDirectory.resolve("reports");

            Files.createDirectories(patientsDirectory);
            Files.createDirectories(reportsDirectory);

            patientCsvFile = dataDirectory.resolve("patients.csv").toString();

            // Initialize CSV file with headers if it doesn't exist
            if (!Files.exists(Paths.get(patientCsvFile))) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(patientCsvFile))) {
                    writer.println("patientId,fullName,documentId,age,sex,email,registrationDate,clinicalNotes,checksumFasta,fileSizeBytes,active,fastaFilename");
                }
            }

            // Load existing patients from CSV
            loadPatientsFromCsv();
            this.diseaseService = new DiseaseService();

        } catch (IOException e) {
            throw new ProtocolException("Failed to initialize PatientService: " + e.getMessage(),
                    ProtocolConstants.ERR_SERVER_ERROR);
        }
    }

    /**
     * Check for the diseases of the patient
     * @param patient the patient to check
     * @param fastaContent the fasta content of the diseases
     */
    private void checkForDiseases(Patient patient, String fastaContent) {
        try {
            System.out.println("=== DISEASE DETECTION START ===");
            System.out.println("Checking diseases for patient: " + patient.getPatientId());

            // Extract just the sequence part (remove header and newlines)
            String sequence = fastaContent.replaceAll("^>.*\\n", "").replaceAll("\\n", "");
            System.out.println("Patient sequence length: " + sequence.length());
            System.out.println("Patient sequence start: " + sequence.substring(0, Math.min(50, sequence.length())));

            // Check for matches with similarity threshold of 0.8 (80%)
            List<DiseaseMatchResult> matches = diseaseService.checkForMatches(sequence, 0.8);
            System.out.println("Found " + matches.size() + " potential matches");

            for (DiseaseMatchResult match : matches) {
                System.out.println("Match detected: " + match.getDescription());
                diseaseService.generateDiseaseReport(patient.getPatientId(), match);

                System.out.println("Disease detected for patient " + patient.getPatientId() +
                        ": " + match.getDescription());
            }

            System.out.println("=== DISEASE DETECTION END ===");

        } catch (Exception e) {
            System.err.println("Error checking for diseases: " + e.getMessage());
        }
    }

    /**
     * Creates a new patient with metadata and genomic data
     * @param metadata JSON object containing patient demographic information
     * @param fastaContent genomic sequence data in FASTA format
     * @return generated patient ID
     * @throws ProtocolException if creation fails due to validation or duplication
     */
    public String createPatient(JSONObject metadata, String fastaContent) throws ProtocolException {
        try {
            // Validate FASTA format
            FastaValidator.validateFasta(fastaContent);

            // Extract metadata
            String documentId = metadata.getString("documentId");

            // Check for duplicate document ID
            if (isDuplicateDocumentId(documentId)) {
                throw new ProtocolException("Duplicate document ID: " + documentId,
                        ProtocolConstants.ERR_DUPLICATE_DOCUMENT);
            }

            // Create patient object
            Patient patient = new Patient(
                    metadata.getString("fullName"),
                    documentId,
                    metadata.getInt("age"),
                    metadata.getString("sex"),
                    metadata.getString("email"),
                    metadata.optString("clinicalNotes", ""),
                    FastaValidator.calculateChecksum(fastaContent),
                    fastaContent.getBytes().length
            );

            // Generate patient ID
            String patientId = "PAT" + String.format("%06d", patientCounter.getAndIncrement());
            patient.setPatientId(patientId);

            // Save FASTA file
            String fastaFilename = saveFastaFile(patientId, fastaContent);
            patient.setFastaFilename(fastaFilename);

            // Store in memory and CSV
            patients.put(patientId, patient);
            savePatientToCsv(patient);

            // === ADD THIS CRITICAL LINE ===
            // Check for disease matches after saving patient
            checkForDiseases(patient, fastaContent);
            // ==============================

            return patientId;

        } catch (Exception e) {
            if (e instanceof ProtocolException) {
                throw (ProtocolException) e;
            }
            throw new ProtocolException("Failed to create patient: " + e.getMessage(),
                    ProtocolConstants.ERR_SERVER_ERROR);
        }
    }

    /**
     * Retrieves patient information by ID
     * @param patientId the patient identifier
     * @return JSON object containing patient data
     * @throws ProtocolException if patient not found or inactive
     */
    public JSONObject getPatient(String patientId) throws ProtocolException {
        Patient patient = patients.get(patientId);
        if (patient == null || !patient.isActive()) {
            throw new ProtocolException("Patient not found: " + patientId,
                    ProtocolConstants.ERR_PATIENT_NOT_FOUND);
        }

        return convertPatientToJson(patient);
    }

    /**
     * Updates an existing patient's information
     * @param patientId the patient identifier
     * @param metadata updated patient metadata
     * @param fastaContent updated genomic data (optional)
     * @throws ProtocolException if patient not found or update fails
     */
    public void updatePatient(String patientId, JSONObject metadata, String fastaContent) throws ProtocolException {
        Patient patient = patients.get(patientId);
        if (patient == null || !patient.isActive()) {
            throw new ProtocolException("Patient not found: " + patientId,
                    ProtocolConstants.ERR_PATIENT_NOT_FOUND);
        }

        try {
            // Update metadata
            if (metadata.has("fullName")) patient.setFullName(metadata.getString("fullName"));
            if (metadata.has("age")) patient.setAge(metadata.getInt("age"));
            if (metadata.has("sex")) patient.setSex(metadata.getString("sex"));
            if (metadata.has("email")) patient.setEmail(metadata.getString("email"));
            if (metadata.has("clinicalNotes")) patient.setClinicalNotes(metadata.getString("clinicalNotes"));

            // Update FASTA only if provided
            if (fastaContent != null && !fastaContent.trim().isEmpty()) {
                FastaValidator.validateFasta(fastaContent);
                patient.setChecksumFasta(FastaValidator.calculateChecksum(fastaContent));
                patient.setFileSizeBytes(fastaContent.getBytes().length);

                String fastaFilename = saveFastaFile(patientId, fastaContent);
                patient.setFastaFilename(fastaFilename);

                // Check for diseases only if FASTA was updated
                checkForDiseases(patient, fastaContent);
            }

            // Update CSV
            updatePatientInCsv(patient);

        } catch (Exception e) {
            throw new ProtocolException("Failed to update patient: " + e.getMessage(),
                    ProtocolConstants.ERR_SERVER_ERROR);
        }
    }

    /**
     * Logically deletes a patient (marks as inactive)
     * @param patientId the patient identifier
     * @throws ProtocolException if patient not found
     */
    public void deletePatient(String patientId) throws ProtocolException, IOException {
        Patient patient = patients.get(patientId);
        if (patient == null) {
            throw new ProtocolException("Patient not found: " + patientId,
                    ProtocolConstants.ERR_PATIENT_NOT_FOUND);
        }

        patient.setActive(false);
        updatePatientInCsv(patient);
    }

    /**
     * Checks if a document ID already exists in active patients
     */
    private boolean isDuplicateDocumentId(String documentId) {
        return patients.values().stream()
                .anyMatch(p -> p.isActive() && p.getDocumentId().equals(documentId));
    }

    /**
     * Saves FASTA content to file
     */
    private String saveFastaFile(String patientId, String fastaContent) throws IOException {
        String filename = patientId + ".fasta";
        Path filePath = patientsDirectory.resolve(filename);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(fastaContent);
        }

        return filename;
    }

    /**
     * Loads patients from CSV file into memory
     */
    private void loadPatientsFromCsv() throws IOException {
        Path path = Paths.get(patientCsvFile);
        if (!Files.exists(path)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }

                String[] values = line.split(",");
                if (values.length >= 12) {
                    Patient patient = getPatient(values);

                    patients.put(patient.getPatientId(), patient);
                    patientCounter.set(Math.max(patientCounter.get(),
                            Integer.parseInt(patient.getPatientId().substring(3)) + 1));
                }
            }
        }
    }

    /**
     * Creates Patient object from CSV values
     */
    private static Patient getPatient(String[] values) {
        Patient patient = new Patient();
        patient.setPatientId(values[0]);
        patient.setFullName(values[1]);
        patient.setDocumentId(values[2]);
        patient.setAge(Integer.parseInt(values[3]));
        patient.setSex(values[4]);
        patient.setEmail(values[5]);
        patient.setRegistrationDate(new Date(Long.parseLong(values[6])));
        patient.setClinicalNotes(values[7]);
        patient.setChecksumFasta(values[8]);
        patient.setFileSizeBytes(Long.parseLong(values[9]));
        patient.setActive(Boolean.parseBoolean(values[10]));
        patient.setFastaFilename(values[11]);
        return patient;
    }

    /**
     * Appends a patient record to CSV file
     */
    private void savePatientToCsv(Patient patient) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(patientCsvFile, true))) {
            writer.println(convertPatientToCsv(patient));
        }
    }

    /**
     * Updates the patient counter based on loaded patient ID
     */
    private void updatePatientInCsv(Patient patient) throws IOException {
        // This is a simplified implementation - in production, you'd want a proper database
        List<String> lines = Files.readAllLines(Paths.get(patientCsvFile));
        try (PrintWriter writer = new PrintWriter(new FileWriter(patientCsvFile))) {
            writer.println(lines.getFirst()); // Write header

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith(patient.getPatientId() + ",")) {
                    writer.println(convertPatientToCsv(patient));
                } else {
                    writer.println(line);
                }
            }
        }
    }

    /**
     * Converts Patient object to CSV line
     */
    private String convertPatientToCsv(Patient patient) {
        return String.join(",",
                patient.getPatientId(),
                patient.getFullName(),
                patient.getDocumentId(),
                String.valueOf(patient.getAge()),
                patient.getSex(),
                patient.getEmail(),
                String.valueOf(patient.getRegistrationDate().getTime()),
                patient.getClinicalNotes(),
                patient.getChecksumFasta(),
                String.valueOf(patient.getFileSizeBytes()),
                String.valueOf(patient.isActive()),
                patient.getFastaFilename()
        );
    }

    /**
     * Converts Patient object to JSON for client response
     */
    private JSONObject convertPatientToJson(Patient patient) {
        JSONObject json = new JSONObject();
        json.put("patientId", patient.getPatientId());
        json.put("fullName", patient.getFullName());
        json.put("documentId", patient.getDocumentId());
        json.put("age", patient.getAge());
        json.put("sex", patient.getSex());
        json.put("email", patient.getEmail());
        json.put("registrationDate", patient.getRegistrationDate().getTime());
        json.put("clinicalNotes", patient.getClinicalNotes());
        json.put("checksumFasta", patient.getChecksumFasta());
        json.put("fileSizeBytes", patient.getFileSizeBytes());
        json.put("active", patient.isActive());
        json.put("fastaFilename", patient.getFastaFilename());
        return json;
    }
}