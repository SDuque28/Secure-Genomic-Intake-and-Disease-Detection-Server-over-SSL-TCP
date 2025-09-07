package com.genomic.common.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Disease - Represents a genetic disease entity in the genomic system
 * Contains disease identification, metadata, severity rating, and associated genomic data reference
 * Used for disease matching and detection against patient genomic sequences
 */
@Setter @Getter
public class Disease {
    private String diseaseId;
    private String name;
    private int severity; // 1-10
    private String fastaFilename;

    /**
     * Default constructor - Creates an empty Disease instance
     * Required for serialization and dependency injection
     */
    public Disease() {}

    /**
     * Parameterized constructor - Creates a fully initialized Disease instance
     * @param diseaseId unique identifier for the disease
     * @param name human-readable disease name
     * @param severity severity rating (1-10 scale)
     * @param fastaFilename filename of the associated FASTA sequence file
     */
    public Disease(String diseaseId, String name, int severity, String fastaFilename) {
        this.diseaseId = diseaseId;
        this.name = name;
        this.severity = severity;
        this.fastaFilename = fastaFilename;
    }
}