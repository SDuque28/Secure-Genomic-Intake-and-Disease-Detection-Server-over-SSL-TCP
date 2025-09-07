package com.genomic.common.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Patient - Represents a patient entity in the genomic system
 * Contains demographic information, clinical data, and genomic file metadata
 * Used for storing and managing patient records with associated genomic data
 */
@Setter @Getter
public class Patient {
    // Getters and Setters
    private String patientId;
    private String fullName;
    private String documentId;
    private int age;
    private String sex; // "M" or "F"
    private String email;
    private Date registrationDate;
    private String clinicalNotes;
    private String checksumFasta;
    private long fileSizeBytes;
    private boolean active;
    private String fastaFilename; // Store FASTA filename for retrieval

    /**
     * Default constructor - Creates a new patient with default values
     * Sets registration date to current time and active status to true
     */
    public Patient() {
        this.registrationDate = new Date();
        this.active = true;
    }

    /**
     * Parameterized constructor - Creates a fully initialized Patient instance
     * @param fullName full legal name of the patient
     * @param documentId unique identification document number
     * @param age age in years (must be positive)
     * @param sex biological sex ("M" or "F")
     * @param email contact email address
     * @param clinicalNotes clinical observations and notes
     * @param checksumFasta SHA-256 checksum of genomic data
     * @param fileSizeBytes size of genomic file in bytes
     */
    public Patient(String fullName, String documentId, int age, String sex,
                   String email, String clinicalNotes, String checksumFasta,
                   long fileSizeBytes) {
        this();
        this.fullName = fullName;
        this.documentId = documentId;
        this.age = age;
        this.sex = sex;
        this.email = email;
        this.clinicalNotes = clinicalNotes;
        this.checksumFasta = checksumFasta;
        this.fileSizeBytes = fileSizeBytes;
    }

    /**
     * Returns a string representation of the patient for debugging and logging
     * Includes key identifying information but excludes sensitive data
     * @return string representation of the patient
     */
    @Override
    public String toString() {
        return "Patient{" +
                "patientId='" + patientId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", documentId='" + documentId + '\'' +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                '}';
    }
}