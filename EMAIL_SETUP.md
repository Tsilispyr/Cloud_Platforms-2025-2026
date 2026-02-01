# Email Configuration Guide

## MailHog Setup (Development)

Το σύστημα χρησιμοποιεί **MailHog** για email testing σε development environment. Το MailHog είναι ένα email testing tool που "παγιδεύει" τα emails χωρίς να τα στέλνει πραγματικά.

### Πώς λειτουργεί:

1. **MailHog Service**: Τρέχει στο port 1025 (SMTP) και 8025 (Web UI)
2. **Backend Configuration**: Το Spring Boot στέλνει emails στο MailHog αντί για πραγματικό SMTP server
3. **View Emails**: Μπορείτε να δείτε όλα τα emails στο MailHog Web UI

### Access MailHog UI:

Αφού τρέξει το `docker-compose.deploy.yml`, ανοίξτε:
- **MailHog Web UI**: http://localhost:8025

Εκεί θα βλέπετε:
- Verification emails (όταν κάνει register ένας χρήστης)
- Login notification emails
- Welcome emails (όταν επιβεβαιωθεί το email)

### Email Types:

1. **Verification Email**: Στέλνεται όταν ένας χρήστης κάνει register
   - Περιέχει link για email verification
   - Link: `http://localhost:8083/verify-email?token=...`

2. **Login Notification**: Στέλνεται κάθε φορά που ένας χρήστης κάνει login
   - Περιέχει ώρα και IP address

3. **Welcome Email**: Στέλνεται όταν επιβεβαιωθεί το email

## Production Setup (Gmail SMTP)

Για production, μπορείτε να χρησιμοποιήσετε Gmail SMTP:

### 1. Δημιουργήστε Gmail App Password:

1. Πηγαίνετε στο Google Account → Security
2. Ενεργοποιήστε 2-Step Verification
3. Δημιουργήστε App Password για "Mail"

### 2. Ορίστε Environment Variables:

```bash
export GMAIL_USER=your_email@gmail.com
export GMAIL_PASS=your_app_password
```

### 3. Αλλάξτε Mail Settings στο docker-compose.deploy.yml:

```yaml
environment:
  SPRING_MAIL_HOST: smtp.gmail.com
  SPRING_MAIL_PORT: 587
  SPRING_MAIL_AUTH: "true"
  SPRING_MAIL_STARTTLS: "true"
  SPRING_MAIL_STARTTLS_REQUIRED: "true"
  GMAIL_USER: ${GMAIL_USER}
  GMAIL_PASS: ${GMAIL_PASS}
```

## Troubleshooting

### Emails δεν στέλνονται:

1. **Ελέγξτε τα backend logs**:
   ```bash
   docker-compose -f docker-compose.deploy.yml logs backend | grep -i mail
   ```

2. **Ελέγξτε αν το MailHog τρέχει**:
   ```bash
   docker ps | grep mailhog
   ```

3. **Ελέγξτε το MailHog UI**: http://localhost:8025

### Common Issues:

- **"Connection refused"**: Το MailHog δεν τρέχει ή δεν είναι accessible
- **"Authentication failed"**: Αν χρησιμοποιείτε Gmail, ελέγξτε τα credentials
- **"Timeout"**: Το MailHog δεν είναι healthy, ελέγξτε τα healthchecks

## Configuration Files

- **Backend**: `Ask/src/main/resources/application.properties`
- **Docker Compose**: `docker-compose.deploy.yml`
- **Email Service**: `Ask/src/main/java/com/example/Ask/Service/EmailService.java`

## Current Configuration (Development)

```yaml
SPRING_MAIL_HOST: mailhog
SPRING_MAIL_PORT: 1025
SPRING_MAIL_AUTH: "false"
SPRING_MAIL_STARTTLS: "false"
FRONTEND_URL: http://localhost:8083
```

## Testing

1. Κάντε register έναν νέο χρήστη
2. Ανοίξτε το MailHog UI: http://localhost:8025
3. Θα δείτε το verification email
4. Κάντε click στο verification link
5. Κάντε login και θα λάβετε login notification email

