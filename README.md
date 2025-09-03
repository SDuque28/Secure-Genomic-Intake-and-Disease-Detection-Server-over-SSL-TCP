# Secure Genomic Intake and Disease Detection Server

A secure, concurrent client-server system for processing and analyzing genomic data over an encrypted channel.

## Overview

This project implements a **TCP-based client and server** that communicate using **SSL/TLS sockets** for secure data transmission. The system is designed to intake patient metadata and genomic sequences in FASTA format, compare them against a known database of disease sequences, and report potential matches.

## Key Features

*   **Secure Communication:** All data exchange is encrypted using SSL/TLS with self-signed certificates.
*   **Custom Protocol:** A bespoke text-based protocol defines all client-server interactions for commands, data transfer, and responses.
*   **Concurrent Server:** A multi-threaded server architecture handles multiple client connections simultaneously.
*   **Genomic Analysis:** The server performs sequence comparison against a pre-loaded disease database to detect potential health risks.
*   **Data Integrity:** Uses checksums (MD5/SHA-256) to verify the integrity of transmitted genomic files.
*   **CRUD Operations:** Supports creating, reading, updating, and logical deletion of patient records.
*   **Comprehensive Logging:** Operations are logged for audit trails, and disease detections are written to a separate report file.

## Technology Stack

*   **Language:** Python
*   **Libraries:** `ssl`, `socket`, `threading`, `csv`, `hashlib`
*   **Version Control:** Git
