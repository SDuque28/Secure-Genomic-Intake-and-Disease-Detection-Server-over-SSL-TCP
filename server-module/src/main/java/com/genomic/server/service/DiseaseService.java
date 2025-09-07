package com.genomic.server.service;

import com.genomic.common.model.Disease;
import com.genomic.common.model.DiseaseMatchResult;
import com.genomic.common.ProtocolException;
import com.genomic.common.ProtocolConstants;
import com.genomic.common.util.SequenceAligner;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * DiseaseService - Manages the disease database and provides genomic sequence matching
 * Loads disease information from catalog and FASTA files, performs sequence alignment,
 * and generates disease detection reports
 */
public class DiseaseService {
    private final ConcurrentMap<String, Disease> diseases = new ConcurrentHashMap<>();
    private final Path diseaseDbDirectory;

    /**
     * Initializes the DiseaseService by loading disease database and creating report directory
     * @throws ProtocolException if initialization fails
     */
    public DiseaseService() throws ProtocolException {
        try {
            diseaseDbDirectory = Paths.get("server-module/src/main/resources/disease_db");
            loadDiseasesFromCatalog();
            if(false){
                System.out.println("=== DISEASE DATABASE LOADED ===");
                System.out.println("Loaded " + diseases.size() + " diseases:");
                for (Disease disease : diseases.values()) {
                    System.out.println(" - " + disease.getDiseaseId() + ": " + disease.getName());
                    try {
                        String sequence = getDiseaseSequence(disease.getDiseaseId());
                        if (sequence != null) {
                            String cleanSeq = sequence.replaceAll("^>.*\\n", "").replaceAll("\\n", "");
                            System.out.println("   Sequence length: " + cleanSeq.length());
                            System.out.println("   Sequence start: " + cleanSeq.substring(0, Math.min(50, cleanSeq.length())));
                        }
                    } catch (IOException e) {
                        System.out.println("   Error reading sequence: " + e.getMessage());
                    }
                }
                System.out.println("=== DISEASE DATABASE END ===");
            }
        } catch (IOException e) {
            throw new ProtocolException("Failed to initialize DiseaseService: " + e.getMessage(),
                    ProtocolConstants.ERR_SERVER_ERROR);
        }
    }

    /**
     * Loads diseases from the catalog CSV file
     * @throws IOException if catalog file cannot be read
     */
    private void loadDiseasesFromCatalog() throws IOException {
        Path catalogFile = diseaseDbDirectory.resolve("catalog.csv");

        if (!Files.exists(catalogFile)) {
            System.out.println("Warning: Disease catalog file not found: " + catalogFile);
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(catalogFile)) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }

                String[] values = line.split(",");
                if (values.length >= 3) {
                    Disease disease = new Disease(
                            values[0].trim(), // diseaseId
                            values[1].trim(), // name
                            Integer.parseInt(values[2].trim()), // severity
                            values.length > 3 ? values[3].trim() : values[0].trim() + ".fasta" // filename
                    );

                    diseases.put(disease.getDiseaseId(), disease);
                }
            }
        }
    }

    /**
     * Gets all loaded diseases
     * @return list of all diseases
     */
    public List<Disease> getAllDiseases() {
        return new ArrayList<>(diseases.values());
    }

    /**
     * Gets a specific disease by ID
     * @param diseaseId the disease identifier
     * @return Disease object, or null if not found
     */
    public Disease getDisease(String diseaseId) {
        return diseases.get(diseaseId);
    }

    /**
     * Retrieves the genomic sequence for a disease
     * @param diseaseId the disease identifier
     * @return FASTA sequence as string, or null if not found
     * @throws IOException if sequence file cannot be read
     */
    public String getDiseaseSequence(String diseaseId) throws IOException {
        Disease disease = diseases.get(diseaseId);
        if (disease == null) {
            return null;
        }

        Path fastaFile = diseaseDbDirectory.resolve(disease.getFastaFilename());
        if (!Files.exists(fastaFile)) {
            return null;
        }

        return Files.readString(fastaFile);
    }

    /**
     * Checks patient genome for matches against all diseases
     * @param patientGenome the patient's genomic sequence
     * @param similarityThreshold minimum similarity score for a match (0.0 to 1.0)
     * @return list of disease matches meeting the similarity threshold
     */
    public List<DiseaseMatchResult> checkForMatches(String patientGenome, double similarityThreshold) {
        List<DiseaseMatchResult> matches = new ArrayList<>();

        System.out.println("Checking against " + diseases.size() + " diseases...");

        for (Disease disease : diseases.values()) {
            try {
                String diseaseSequence = getDiseaseSequence(disease.getDiseaseId());
                if (diseaseSequence != null) {
                    String cleanDiseaseSeq = diseaseSequence.replaceAll("^>.*\\n", "").replaceAll("\\n", "");
                    String cleanPatientSeq = patientGenome.replaceAll("^>.*\\n", "").replaceAll("\\n", "");

                    System.out.println("Comparing with " + disease.getDiseaseId() + " - " + disease.getName());
                    System.out.println("Disease sequence length: " + cleanDiseaseSeq.length());
                    System.out.println("Patient sequence length: " + cleanPatientSeq.length());

                    if (SequenceAligner.isPotentialMatch(cleanPatientSeq, cleanDiseaseSeq, similarityThreshold)) {
                        double similarity = calculateSimilarity(cleanPatientSeq, cleanDiseaseSeq);
                        System.out.println("Similarity with " + disease.getDiseaseId() + ": " + similarity);

                        if (similarity >= similarityThreshold) {
                            matches.add(new DiseaseMatchResult(disease, similarity));
                            System.out.println("✅ MATCH FOUND: " + disease.getName() + " - similarity: " + similarity);
                        }
                    } else {
                        System.out.println("No potential match with " + disease.getDiseaseId());
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading disease sequence for " + disease.getDiseaseId() + ": " + e.getMessage());
            }
        }
        return matches;
    }

    /**
     * Calculates the similarities between two genome sequences
     * @param sequence1 of the patient
     * @param sequence2 of the list of results
     * @return the calculated similarity
     */
    private double calculateSimilarity(String sequence1, String sequence2) {
        return SequenceAligner.calculateSimilarity(sequence1, sequence2);
    }

    /**
     * Generates a disease detection report and appends to CSV file
     * @param patientId the patient identifier
     * @param matchResult the disease match result
     */
    public void generateDiseaseReport(String patientId, DiseaseMatchResult matchResult) {
        try {
            Path reportFile = Paths.get("server-module/src/main/resources/data/reports/disease_detections.csv");

            // Create header if file doesn't exist
            if (!Files.exists(reportFile)) {
                Files.createDirectories(reportFile.getParent());
                Files.writeString(reportFile, "patientId,diseaseId,diseaseName,severity,similarity,detectionDate,description\n");
            }

            String reportLine = String.format("%s,%s,%s,%d,%.4f,%s,%s\n",
                    patientId,
                    matchResult.getDisease().getDiseaseId(),
                    escapeCsv(matchResult.getDisease().getName()),
                    matchResult.getDisease().getSeverity(),
                    matchResult.getSimilarity(),
                    java.time.LocalDateTime.now().toString(),
                    escapeCsv(matchResult.getDescription()));

            Files.writeString(reportFile, reportLine, java.nio.file.StandardOpenOption.APPEND);

        } catch (IOException e) {
            System.err.println("Error writing disease report: " + e.getMessage());
        }
    }

    /**
     * Escapes CSV special characters in a string
     * @param value the string to escape
     * @return CSV-safe string
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        // Escape quotes and wrap in quotes if contains comma
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}