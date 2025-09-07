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

public class DiseaseService {
    private final ConcurrentMap<String, Disease> diseases = new ConcurrentHashMap<>();
    private final Path diseaseDbDirectory;

    public DiseaseService() throws ProtocolException {
        try {
            diseaseDbDirectory = Paths.get("server-module/src/main/resources/disease_db");
            loadDiseasesFromCatalog();
            System.out.println("Loaded " + diseases.size() + " diseases from catalog");

        } catch (IOException e) {
            throw new ProtocolException("Failed to initialize DiseaseService: " + e.getMessage(),
                    ProtocolConstants.ERR_SERVER_ERROR);
        }
    }

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

    public List<Disease> getAllDiseases() {
        return new ArrayList<>(diseases.values());
    }

    public Disease getDisease(String diseaseId) {
        return diseases.get(diseaseId);
    }

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

    public List<DiseaseMatchResult> checkForMatches(String patientGenome, double similarityThreshold) {
        List<DiseaseMatchResult> matches = new ArrayList<>();

        for (Disease disease : diseases.values()) {
            try {
                String diseaseSequence = getDiseaseSequence(disease.getDiseaseId());
                if (diseaseSequence != null) {
                    String cleanDiseaseSeq = diseaseSequence.replaceAll("^>.*\\n", "").replaceAll("\\n", "");
                    String cleanPatientSeq = patientGenome.replaceAll("^>.*\\n", "").replaceAll("\\n", "");

                    if (SequenceAligner.isPotentialMatch(cleanPatientSeq, cleanDiseaseSeq, similarityThreshold)) {
                        double similarity = calculateSimilarity(cleanPatientSeq, cleanDiseaseSeq);
                        if (similarity >= similarityThreshold) {
                            matches.add(new DiseaseMatchResult(disease, similarity));
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading disease sequence for " + disease.getDiseaseId() + ": " + e.getMessage());
            }
        }
        return matches;
    }

    private double calculateSimilarity(String sequence1, String sequence2) {
        return SequenceAligner.calculateSimilarity(sequence1, sequence2);
    }

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

    private String escapeCsv(String value) {
        if (value == null) return "";
        // Escape quotes and wrap in quotes if contains comma
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}