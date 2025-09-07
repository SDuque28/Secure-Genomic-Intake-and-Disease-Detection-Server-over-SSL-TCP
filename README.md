# Genomic Intake and Disease Detection System

A secure client-server system for managing patient genomic data and detecting disease patterns using SSL/TLS encrypted communication.

## 🧬 System Overview

This system provides a secure platform for:
- Storing patient metadata and genomic FASTA sequences
- Detecting disease patterns through genomic sequence comparison
- Managing patient records with full CRUD operations
- Secure SSL/TLS communication between client and server

## 🏗️ Architecture

```
genomic-server-project/
├── client-module/          # Client application
├── server-module/          # Server application  
├── common-module/          # Shared classes and utilities
└── configuration/          # SSL certificates and properties
```

## 📋 Prerequisites

- **Java 17+** (JDK required)
- **Maven 3.6+** (for building)
- **OpenSSL** (for certificate management, optional)

## 🔐 SSL Certificate Setup

### Generate Self-Signed Certificate (Development):

```bash
# Generate PKCS12 certificate
openssl req -newkey rsa:2048 -nodes -keyout key.pem -x509 -days 365 -out certificate.pem
openssl pkcs12 -inkey key.pem -in certificate.pem -export -out tcp_key.p12

# Password: changeit (or your chosen password)
```

### Certificate Placement:

Place the generated `tcp_key.p12` file in both:
- `client-module/src/main/resources/`
- `server-module/src/main/resources/`

## ⚙️ Configuration

Update `configuration.properties` in both client and server modules:

```properties
# Server configuration
SSL_CERTIFICATE_ROUTE=tcp_key.p12
SSL_PASSWORD=your_password_here
SERVER_PORT=2020
SERVER_ADDRESS=localhost
```

## 🚀 Building the Project

### Option 1: Build All Modules

```bash
# From root directory
mvn clean compile

# Or create JAR files
mvn clean package
```

### Option 2: Build Individual Modules

```bash
# Build common module first
cd common-module
mvn clean install

# Build server
cd ../server-module  
mvn clean compile

# Build client
cd ../client-module
mvn clean compile
```

## 🖥️ Running the System

### Step 1: Start the Server

```bash
cd server-module
mvn exec:java -Dexec.mainClass="com.genomic.server.Main"

# Or if packaged:
java -jar target/server-module-1.0.0.jar
```

**Expected Output:**
```
SSL Context created successfully with protocol: TLS
Services initialized successfully
SSL Server started on port: 2020
Ready to handle multiple simultaneous connections...
```

### Step 2: Run the Client

```bash
cd client-module
mvn exec:java -Dexec.mainClass="com.genomic.client.EnhancedClient"

# Or for basic client:
mvn exec:java -Dexec.mainClass="com.genomic.client.Main"
```

## 🎯 Usage Examples

### Interactive Client (Recommended):

```bash
# Follow the menu prompts:
# 1. Create Patient
# 2. Get Patient Information  
# 3. Update Patient
# 4. Delete Patient
# 5. Batch Operations
# 6. Exit
```

### Batch Operations:

The system can create multiple test patients automatically:
- Choose option 5 → Create Multiple Test Patients
- Enter number of patients to create (1-100)
- System will create patients with sequential numbering

### Disease Detection:

When creating or updating patients with genomic data:
- System automatically compares against disease database
- Generates reports for matches above 80% similarity
- Reports saved to `server-module/src/main/resources/data/reports/`

## 📊 Protocol Commands

The system uses a custom text-based protocol:

| Command | Format | Description |
|---------|--------|-------------|
| CREATE_PATIENT | `CREATE_PATIENT|{metadata_json}|{fasta_content}` | Create new patient |
| GET_PATIENT | `GET_PATIENT|{patient_id}` | Retrieve patient data |
| UPDATE_PATIENT | `UPDATE_PATIENT|{patient_id}|{metadata_json}[|{fasta_content}]` | Update patient |
| DELETE_PATIENT | `DELETE_PATIENT|{patient_id}` | Delete patient |

## 🗂️ File Structure

### Data Files:
- **Patients CSV**: `server-module/src/main/resources/data/patients.csv`
- **FASTA Files**: `server-module/src/main/resources/data/patients/`
- **Disease Reports**: `server-module/src/main/resources/data/reports/disease_detections.csv`
- **Server Logs**: `server-module/src/main/resources/logs/server.log`

### Disease Database:
- **Catalog**: `server-module/src/main/resources/disease_db/catalog.csv`
- **FASTA Sequences**: `server-module/src/main/resources/disease_db/disease_*.fasta`

## 🧪 Testing

### Manual Testing:

1. **Create Test Patients**: Use batch operations to create multiple patients
2. **Verify Data**: Check CSV files and patient directory
3. **Test Disease Detection**: Use sample FASTA sequences that match disease patterns
4. **Check Reports**: Verify disease detection reports are generated

### Sample FASTA for Testing:

```fasta
>test_patient_cf
ACGTACGTGGCCTTAAACCGGTAGCTAGCTAGGCTAGCTAGCTAGCTAGCTAGCTAGCGATCGATCGTAA
ACGTACGTGGCCTTAAACCGGTAGCTAGCTAGGCTAGCTAGCTAGCTAGCTAGCTAGCGATCGATCGTAA
GCTAGCTAGCTAAGCTAGCTAGCGATCGATCGTAAACGTACGTGGCCTTAAACCGGTAGCTAGCTAGGCTA
```

## 🔧 Troubleshooting

### Common Issues:

1. **SSL Handshake Failed**:
    - Verify certificate exists in both client and server resources
    - Check password in configuration.properties matches certificate password

2. **Connection Refused**:
    - Ensure server is running before starting client
    - Check firewall settings on port 2020

3. **File Permission Errors**:
    - Ensure write permissions for data directories

4. **Duplicate Document ID**:
    - The system now automatically handles sequential numbering

### Debug Mode:

Enable debug logging by modifying the Logger class:
```java
Logger.setMinimumLogLevel(Logger.LogLevel.DEBUG);
```

## 📈 Performance Monitoring

The system includes performance monitoring:
- Request counts and timing
- Connection statistics
- Error tracking
- View stats with: `PerformanceMonitor.printStats()`

## 🛡️ Security Features

- SSL/TLS 1.2+ encryption
- Certificate-based authentication
- Secure data transmission
- Input validation and sanitization
- No sensitive data logging

## 📝 API Documentation

### Patient Data Model:
```java
{
  "patientId": "PAT000001",
  "fullName": "John Doe",
  "documentId": "DOC123456",
  "age": 35,
  "sex": "M",
  "email": "john.doe@example.com",
  "clinicalNotes": "Patient notes",
  "checksumFasta": "sha256_hash",
  "fileSizeBytes": 1024,
  "active": true,
  "fastaFilename": "PAT000001.fasta"
}
```

### Response Format:
**Success**: `SUCCESS|{json_data}`
**Error**: `ERROR|{"code":"ERROR_CODE","message":"description"}`

## 🚨 Error Codes

| Code | Description | HTTP Equivalent |
|------|-------------|-----------------|
| INVALID_FORMAT | Malformed request | 400 Bad Request |
| PATIENT_NOT_FOUND | Patient doesn't exist | 404 Not Found |
| DUPLICATE_DOCUMENT | Document ID already exists | 409 Conflict |
| INVALID_FASTA | Invalid genomic format | 422 Unprocessable |
| SERVER_ERROR | Internal server error | 500 Internal Error |

## 📊 Logging

Log files are stored in:
- `server-module/src/main/resources/logs/server.log`
- Console output for both client and server

Log levels: DEBUG, INFO, WARNING, ERROR

## 🔄 Version Information

- **Java**: 17+
- **Maven**: 3.6+
- **Protocol Version**: 1.0.0
- **SSL**: TLS 1.2/1.3

## 📞 Support

For issues and questions:
1. Check troubleshooting section above
2. Verify certificate configuration
3. Check server logs for detailed error messages
4. Ensure all prerequisites are met

## 📄 License

This project is for educational/demonstration purposes. Ensure proper licensing for production use.