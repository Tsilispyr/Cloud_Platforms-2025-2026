# Backend API Documentation

## Overview
Το Backend API είναι γραμμένο σε Spring Boot και διαχειρίζεται τη λογική της εφαρμογής, την επικοινωνία με τη βάση δεδομένων, και το messaging.

## Configuration

### Environment Variables (Gmail)

Για να λειτουργήσει η αποστολή email (Verification, Welcome, Notifications), απαιτούνται τα εξής:

```bash
# Linux/Mac
export GMAIL_USER="your_email@gmail.com"
export GMAIL_PASS="your_16_digit_app_password"

# Windows PowerShell
$env:GMAIL_USER="your_email@gmail.com"
$env:GMAIL_PASS="your_16_digit_app_password"
```

> **Σημείωση**: Χρειάζεστε **Gmail App Password** (όχι το κανονικό password).
> 1. Google Account -> Security -> 2-Step Verification -> App passwords.
> 2. Δημιουργήστε νέο για "Mail".

### ThingsBoard Integration

Για να συνδέσετε το Backend με το ThingsBoard:
1. Login στο ThingsBoard (http://localhost:9090) με `sysadmin@thingsboard.org` / `sysadmin`.
2. Δημιουργήστε ένα Device (π.χ. "Pet Device") και αντιγράψτε το **Access Token**.
3. Ορίστε το token ως environment variable:
   ```bash
   export THINGSBOARD_TOKEN="your_access_token_here"
   ```
4. Επανεκκινήστε το backend.

## Features

- **Automatic Animal Data**: Όταν προστίθεται νέο ζώο, τα δεδομένα στέλνονται στο ThingsBoard.
- **Device Simulator**: Το backend στέλνει sample telemetry data κάθε 30 δευτερόλεπτα.
- **RabbitMQ**: Χρησιμοποιείται για ασύγχρονη επικοινωνία.