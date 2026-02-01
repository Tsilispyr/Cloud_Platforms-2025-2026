# Τεύχος Σχεδιασμού (Design Document)

## 1. Περιγραφή Προβλήματος
Σκοπός του έργου είναι η δημιουργία ενός ολοκληρωμένου συστήματος διαχείρισης δεδομένων για υιοθεσία κατοικιδίων. Το σύστημα καλείται να λύσει τα εξής προβλήματα:
- Ασφαλής αποθήκευση και ανάκτηση πληροφοριών ζώων συντροφιάς.
- Διαχείριση πολυμέσων (φωτογραφίες) σε κλιμακώσιμο αποθηκευτικό χώρο.
- Παρακολούθηση δεδομένων τηλεμετρίας (IoT) σε πραγματικό χρόνο.
- Αυτοματοποιημένη ανάπτυξη (Deployment) σε περιβάλλοντα Cloud/Kubernetes.

## 2. Αρχιτεκτονική Συστήματος
Η λύση ακολουθεί αρχιτεκτονική **Microservices**, όπου κάθε λειτουργία εκτελείται σε απομονωμένο container.

### Δομικά Στοιχεία (Components)
1.  **Frontend**: Για την αλληλεπίδραση με τον χρήστη.
2.  **Backend API**: Κεντρική λογική, διαχείριση αιτημάτων και ενορχήστρωση υπηρεσιών.
3.  **Database**: Σχεσιακή βάση για δομημένα δεδομένα.
4.  **Object Storage**: Αποθήκευση μη δομημένων δεδομένων (εικόνες).
5.  **Identity Provider**: Κεντρική διαχείριση ταυτοτήτων και πρόσβασης.
6.  **Message Broker**: Ασύγχρονη επικοινωνία μεταξύ υπηρεσιών.
7.  **IoT Platform**: Οπτικοποίηση και διαχείριση τηλεμετρίας.

## 3. Περιγραφή Λύσης & Υλοποίηση

### 3.1 Frontend (Vue.js)
- **Τεχνολογία**: Vue.js 3 με Vite.
- **Λειτουργία**: Παρέχει φόρμες εγγραφής, λίστες ζώων και διαχείριση προφίλ. Επικοινωνεί με το Backend μέσω REST API.

### 3.2 Backend (Spring Boot)
- **Τεχνολογία**: Java 17, Spring Boot 3.
- **Λειτουργία**: 
    - Exposes REST endpoints.
    - Συνδέεται με PostgreSQL για metadata.
    - Χρησιμοποιεί MinIO SDK για upload εικόνων.
    - Στέλνει events στο RabbitMQ (π.χ. `pet.created`, `telemetry.update`).
    - Υλοποιεί SMTP client για αποστολή emails (Gmail).

### 3.3 Δεδομένα & Αποθήκευση
- **PostgreSQL**: Κύρια βάση δεδομένων (Users, Pets info).
- **MinIO**: S3-compatible storage για τις φωτογραφίες των ζώων.

### 3.4 Ασφάλεια (Keycloak)
- **Keycloak**: Διαχειρίζεται το authentication (OIDC). Το Backend επικυρώνει τα JWT tokens.

### 3.5 IoT & Messaging
- **RabbitMQ**: Μεταφέρει μηνύματα μεταξύ Backend και ThingsBoard/Node-RED.
- **ThingsBoard**: Λαμβάνει δεδομένα από το RabbitMQ και τα εμφανίζει σε Dashboards.
- **Node-RED**: Χρησιμοποιείται για simulation ροών και automation.

## 4. Διαδικασία Ανάπτυξης (Deployment)
Η εφαρμογή είναι containerized με Docker.
- **Docker Compose**: Για τοπική ανάπτυξη και δοκιμές (`docker-compose.deploy.yml`).
- **Kubernetes**: Για production deployment με manifests (`k8s/` folder).