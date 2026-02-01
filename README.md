# Τεύχος Σχεδιασμού - Pet Adoption System

## 1. Περιγραφή Προβλήματος

### 1.1 Σκοπός του Συστήματος
Το **Pet Adoption System** είναι ένα ολοκληρωμένο σύστημα διαχείρισης για την υιοθεσία κατοικιδίων. Το σύστημα έχει σχεδιαστεί για να διευκολύνει τη διαδικασία υιοθεσίας, να διαχειρίζεται πληροφορίες ζώων και να παρέχει πλατφόρμα επικοινωνίας μεταξύ πολιτών, κτηνιάτρων, καταφυγίων και διαχειριστών.

### 1.2 Κύρια Προβλήματα που Λύνει

#### 1.2.1 Διαχείριση Πληροφοριών Ζώων
- **Πρόβλημα**: Χρειάζεται ασφαλής αποθήκευση και ανάκτηση πληροφοριών για τα διαθέσιμα ζώα (όνομα, ηλικία, φύλο, είδος, φωτογραφίες).
- **Λύση**: Σχεσιακή βάση δεδομένων (PostgreSQL) για δομημένα δεδομένα και MinIO για αποθήκευση εικόνων.

#### 1.2.2 Διαχείριση Πολυμέσων
- **Πρόβλημα**: Χρειάζεται αποθήκευση φωτογραφιών σε κλιμακώσιμο αποθηκευτικό χώρο με πρόσβαση μέσω URL.
- **Λύση**: MinIO (S3-compatible object storage) για αποθήκευση εικόνων με presigned URLs.

#### 1.2.3 Ασφάλεια και Ελέγχος Πρόσβασης
- **Πρόβλημα**: Χρειάζεται ασφαλής διαχείριση ταυτοτήτων και έλεγχος πρόσβασης με βάση τους ρόλους (Admin, Doctor, Citizen, Shelter).
- **Λύση**: Keycloak για OAuth2/OIDC authentication και JWT tokens για authorization.

#### 1.2.4 Παρακολούθηση Τηλεμετρίας (IoT)
- **Πρόβλημα**: Χρειάζεται παρακολούθηση δεδομένων τηλεμετρίας σε πραγματικό χρόνο για IoT devices.
- **Λύση**: ThingsBoard για οπτικοποίηση και διαχείριση τηλεμετρίας, RabbitMQ για messaging.

#### 1.2.5 Αυτοματοποιημένη Ανάπτυξη
- **Πρόβλημα**: Χρειάζεται εύκολη και αυτοματοποιημένη ανάπτυξη σε περιβάλλοντα Cloud/Kubernetes.
- **Λύση**: Docker containers και Kubernetes manifests για container orchestration.

#### 1.2.6 Ειδοποιήσεις και Επικοινωνία
- **Πρόβλημα**: Χρειάζεται αυτοματοποιημένη αποστολή emails για ειδοποιήσεις (verification, welcome, notifications).
- **Λύση**: SMTP integration (Gmail) για αποστολή emails.

## 2. Αρχιτεκτονική Συστήματος

### 2.1 Αρχιτεκτονική Μοτίβο
Το σύστημα ακολουθεί την αρχιτεκτονική **Microservices**, όπου κάθε λειτουργία εκτελείται σε απομονωμένο container. Αυτό επιτρέπει:
- **Αποσύνδεση (Decoupling)**: Κάθε υπηρεσία λειτουργεί ανεξάρτητα
- **Κλιμακωσιμότητα (Scalability)**: Κάθε υπηρεσία μπορεί να κλιμακωθεί ξεχωριστά
- **Αποκατάσταση (Resilience)**: Η αποτυχία μιας υπηρεσίας δεν επηρεάζει τις άλλες
- **Τεχνολογική Ελευθερία**: Κάθε υπηρεσία μπορεί να χρησιμοποιεί διαφορετικές τεχνολογίες

### 2.2 Δομικά Στοιχεία (Components)

#### 2.2.1 Frontend (Vue.js)
- **Τεχνολογία**: Vue.js 3, Vite, Pinia (state management), Vue Router
- **Ρόλος**: Παρέχει την διεπαφή χρήστη (UI) για όλες τις λειτουργίες
- **Port**: 8083
- **Χαρακτηριστικά**:
  - Single Page Application (SPA)
  - Responsive design
  - JWT-based authentication
  - REST API integration

#### 2.2.2 Backend API (Spring Boot)
- **Τεχνολογία**: Java 17, Spring Boot 3.3.8, Spring Security, Spring Data JPA
- **Ρόλος**: Κεντρική λογική, διαχείριση αιτημάτων, ενορχήστρωση υπηρεσιών
- **Port**: 8080
- **Χαρακτηριστικά**:
  - RESTful API
  - JWT authentication
  - Database integration (PostgreSQL)
  - File upload (MinIO)
  - Email service (SMTP)
  - IoT integration (ThingsBoard)

#### 2.2.3 Database (PostgreSQL)
- **Ρόλος**: Σχεσιακή βάση για δομημένα δεδομένα
- **Port**: 5432
- **Σχήματα**:
  - Users, Roles, User_Roles
  - Animals
  - Requests
  - Keycloak tables

#### 2.2.4 Object Storage (MinIO)
- **Ρόλος**: Αποθήκευση μη δομημένων δεδομένων (εικόνες)
- **Ports**: 9000 (API), 9001 (Console)
- **Χαρακτηριστικά**:
  - S3-compatible API
  - Presigned URLs
  - Bucket-based organization

#### 2.2.5 Identity Provider (Keycloak)
- **Ρόλος**: Κεντρική διαχείριση ταυτοτήτων και πρόσβασης
- **Port**: 8081
- **Χαρακτηριστικά**:
  - OAuth2/OIDC
  - JWT token generation
  - Role-based access control (RBAC)
  - User management

#### 2.2.6 Message Broker (RabbitMQ)
- **Ρόλος**: Ασύγχρονη επικοινωνία μεταξύ υπηρεσιών
- **Ports**: 5672 (AMQP), 15672 (Management UI)
- **Χρήσεις**:
  - Event-driven communication
  - ThingsBoard integration
  - Node-RED integration

#### 2.2.7 IoT Platform (ThingsBoard) (Υλοποίηση μόνο λογικής, δεν υολοποιήθηκε σε λειτουργικό επίπεδο)
- **Ρόλος**: Οπτικοποίηση και διαχείριση τηλεμετρίας
- **Port**: 9090
- **Χαρακτηριστικά**:
  - Device management
  - Telemetry data visualization
  - Dashboards
  - RabbitMQ integration

#### 2.2.8 Automation Platform (Node-RED)
- **Ρόλος**: Simulation ροών και automation
- **Port**: 1880
- **Χαρακτηριστικά**:
  - Visual flow-based programming
  - RabbitMQ integration
  - ThingsBoard integration

#### 2.2.9 Email Service (SMTP/Gmail)
- **Ρόλος**: Αποστολή emails (verification, notifications)
- **Ενσωμάτωση**: Spring Mail (SMTP)

## 3. Σενάρια Χρήσης (Use Cases)

### 3.1 Εγγραφή Νέου Χρήστη

**Actor**: Πολίτης (Citizen)

**Προϋποθέσεις**: Ο χρήστης δεν έχει λογαριασμό

**Βασική Ροή**:
1. Ο χρήστης επιλέγει "Εγγραφή"
2. Συμπληρώνει τη φόρμα (username, email, password)
3. Το Frontend στέλνει POST request στο `/api/auth/register`
4. Το Backend:
   - Επικυρώνει τα δεδομένα
   - Ελέγχει αν το username/email υπάρχει ήδη
   - Δημιουργεί νέο User με verification token
   - Αποθηκεύει στη βάση δεδομένων
   - Στέλνει verification email μέσω SMTP
5. Ο χρήστης λαμβάνει email με verification link
6. Κάνει κλικ στο link
7. Το Backend επικυρώνει το token και ενεργοποιεί τον λογαριασμό
8. Στέλνεται welcome email

**Εναλλακτικές Ροές**:
- 3a. Το username/email υπάρχει ήδη → Εμφάνιση error message
- 3b. Invalid email format → Εμφάνιση error message
- 3c. Password πολύ μικρό → Εμφάνιση error message
- 5a. Το email δεν φτάνει → Ο χρήστης μπορεί να ζητήσει resend

### 3.2 Σύνδεση Χρήστη (Login)

**Actor**: Επαληθευμένος Χρήστης

**Προϋποθέσεις**: Ο χρήστης έχει ενεργοποιημένο λογαριασμό

**Βασική Ροή**:
1. Ο χρήστης επιλέγει "Σύνδεση"
2. Συμπληρώνει username και password
3. Το Frontend στέλνει POST request στο `/api/auth/login`
4. Το Backend:
   - Ελέγχει αν ο χρήστης είναι verified
   - Επικυρώνει credentials
   - Δημιουργεί JWT token
   - Ενημερώνει last_login timestamp
   - Στέλνει login notification email
5. Το Frontend αποθηκεύει το token
6. Ο χρήστης ανακατευθύνεται στο dashboard ανάλογα με τον ρόλο του

**Εναλλακτικές Ροές**:
- 4a. Ο χρήστης δεν είναι verified → Εμφάνιση μηνύματος "Please verify your email"
- 4b. Invalid credentials → Εμφάνιση error message

### 3.3 Προσθήκη Νέου Ζώου

**Actor**: Shelter (Καταφύγιο)

**Προϋποθέσεις**: Ο χρήστης είναι συνδεδεμένος με ρόλο SHELTER

**Βασική Ροή**:
1. Ο χρήστης επιλέγει "Προσθήκη Ζώου"
2. Συμπληρώνει τη φόρμα (όνομα, είδος, ηλικία, φύλο)
3. Ανεβάζει φωτογραφία
4. Το Frontend στέλνει POST request στο `/api/animals` με multipart/form-data
5. Το Backend:
   - Επικυρώνει τα δεδομένα
   - Ανεβάζει την εικόνα στο MinIO
   - Δημιουργεί Animal entity στη βάση
   - Στέλνει notification email σε όλους τους verified users
   - Στέλνει δεδομένα στο ThingsBoard ως telemetry
6. Το Frontend ενημερώνει τη λίστα ζώων

**Εναλλακτικές Ροές**:
- 3a. Η εικόνα είναι πολύ μεγάλη → Εμφάνιση error message
- 3b. Invalid file format → Εμφάνιση error message
- 5a. Το MinIO δεν είναι διαθέσιμο → Εμφάνιση error message

### 3.4 Αίτηση Υιοθεσίας

**Actor**: Citizen (Πολίτης)

**Προϋποθέσεις**: Ο χρήστης είναι συνδεδεμένος με ρόλο CITIZEN

**Βασική Ροή**:
1. Ο χρήστης περιηγείται στη λίστα ζώων
2. Επιλέγει ένα ζώο
3. Κάνει κλικ στο "Αίτηση Υιοθεσίας"
4. Το Frontend στέλνει PUT request στο `/api/animals/Request/{id}`
5. Το Backend:
   - Επικυρώνει ότι το ζώο είναι διαθέσιμο
   - Δημιουργεί Request entity
   - Σημειώνει το ζώο ως "requested"
   - Στέλνει confirmation email στον χρήστη
6. Το Frontend ενημερώνει την κατάσταση του ζώου

**Εναλλακτικές Ροές**:
- 4a. Το ζώο έχει ήδη αιτηθεί → Εμφάνιση error message
- 4b. Ο χρήστης έχει ήδη υποβάλει αίτηση → Εμφάνιση error message

### 3.5 Έγκριση Αίτησης από Κτηνίατρο

**Actor**: Doctor (Κτηνίατρος)

**Προϋποθέσεις**: Ο χρήστης είναι συνδεδεμένος με ρόλο DOCTOR

**Βασική Ροή**:
1. Ο κτηνίατρος περιηγείται στη λίστα αιτημάτων
2. Επιλέγει ένα αίτημα
3. Εξετάζει τις πληροφορίες
4. Κάνει κλικ στο "Έγκριση"
5. Το Frontend στέλνει PUT request στο `/api/requests/{id}/approve-doctor`
6. Το Backend:
   - Ενημερώνει το Request με DocApproved = 1
   - Ελέγχει αν χρειάζεται και admin approval
   - Στέλνει notification email
7. Το Frontend ενημερώνει την κατάσταση

**Εναλλακτικές Ροές**:
- 4a. Ο κτηνίατρος απορρίπτει → Ενημέρωση με DocApproved = -1 και αποστολή rejection email

### 3.6 Έγκριση Αίτησης από Admin

**Actor**: Admin (Διαχειριστής)

**Προϋποθέσεις**: Ο χρήστης είναι συνδεδεμένος με ρόλο ADMIN

**Βασική Ροή**:
1. Ο admin περιηγείται στη λίστα αιτημάτων
2. Επιλέγει ένα αίτημα που έχει εγκριθεί από κτηνίατρο
3. Κάνει κλικ στο "Έγκριση"
4. Το Frontend στέλνει PUT request στο `/api/requests/{id}/approve-admin`
5. Το Backend:
   - Ενημερώνει το Request με AdminApproved = 1
   - Μετατρέπει το Request σε Animal
   - Στέλνει approval email στον αιτούντα
6. Το Frontend ενημερώνει τη λίστα

### 3.7 Ολοκλήρωση Υιοθεσίας

**Actor**: Admin ή Shelter

**Προϋποθέσεις**: Το αίτημα έχει εγκριθεί από admin

**Βασική Ροή**:
1. Ο admin επιλέγει ένα εγκεκριμένο αίτημα
2. Κάνει κλικ στο "Ολοκλήρωση Υιοθεσίας"
3. Το Frontend στέλνει POST request στο `/api/animals/{id}/accept-adoption`
4. Το Backend:
   - Στέλνει confirmation email στον υιοθετούντα
   - Διαγράφει το Animal από τη βάση
   - Διαγράφει την εικόνα από το MinIO
5. Το Frontend ενημερώνει τη λίστα

### 3.8 Προβολή Τηλεμετρίας (IoT)

**Actor**: Admin, Doctor

**Προϋποθέσεις**: Ο χρήστης έχει πρόσβαση στο ThingsBoard

**Βασική Ροή**:
1. Ο χρήστης ανοίγει το ThingsBoard dashboard
2. Βλέπει real-time telemetry data (temperature, heartRate, activityLevel, batteryLevel)
3. Το Backend στέλνει telemetry data κάθε 30 δευτερόλεπτα
4. Το ThingsBoard εμφανίζει τα δεδομένα σε graphs και charts

## 4. Περιγραφή Υλοποίησης

### 4.1 Frontend Implementation

#### 4.1.1 Τεχνολογίες
- **Vue.js 3**: Progressive JavaScript framework
- **Vite**: Build tool και development server
- **Pinia**: State management
- **Vue Router**: Client-side routing
- **Axios**: HTTP client για API calls

#### 4.1.2 Δομή Project
```
frontend/
├── src/
│   ├── api.js              # Axios configuration και interceptors
│   ├── main.js             # Application entry point
│   ├── App.vue             # Root component
│   ├── router.js           # Route definitions
│   ├── stores/
│   │   ├── auth.js         # Authentication state
│   │   └── application.js # Application state
│   ├── views/              # Page components
│   │   ├── Login.vue
│   │   ├── Register.vue
│   │   ├── Animals.vue
│   │   ├── Requests.vue
│   │   └── ...
│   └── components/         # Reusable components
│       ├── Navbar.vue
│       ├── AnimalList.vue
│       └── ...
```

#### 4.1.3 Authentication Flow
1. Ο χρήστης κάνει login → λαμβάνει JWT token
2. Το token αποθηκεύεται στο Pinia store
3. Κάθε API request περιλαμβάνει το token στο Authorization header
4. Αν το token είναι expired ή invalid → redirect στο login

#### 4.1.4 API Integration
- Όλα τα API calls γίνονται μέσω του `api.js` που χρησιμοποιεί Axios
- Interceptors προσθέτουν αυτόματα το JWT token
- Error handling για 401 (unauthorized) → logout

### 4.2 Backend Implementation

#### 4.2.1 Τεχνολογίες
- **Spring Boot 3.3.8**: Application framework
- **Java 17**: Programming language
- **Spring Security**: Security framework
- **Spring Data JPA**: Database access
- **PostgreSQL**: Relational database
- **MinIO SDK**: Object storage client
- **Spring Mail**: Email service
- **JWT (jjwt)**: Token generation

#### 4.2.2 Δομή Project
```
Ask/
├── src/main/java/com/example/Ask/
│   ├── config/             # Configuration classes
│   │   ├── SecurityConfig.java
│   │   ├── JwtUtil.java
│   │   ├── MinioConfig.java
│   │   └── ...
│   ├── Controllers/        # REST controllers
│   │   ├── AuthController.java
│   │   ├── AnimalController.java
│   │   ├── RequestController.java
│   │   └── ...
│   ├── Entities/           # JPA entities
│   │   ├── User.java
│   │   ├── Animal.java
│   │   ├── Request.java
│   │   └── ...
│   ├── Repositories/       # Data access layer
│   │   ├── UserRepository.java
│   │   ├── AnimalRepository.java
│   │   └── ...
│   └── Service/            # Business logic
│       ├── UserService.java
│       ├── AnimalService.java
│       ├── EmailService.java
│       ├── FileStorageService.java
│       └── ThingsboardService.java
```

#### 4.2.3 Security Implementation
- **JWT Authentication**: Custom filter (`JwtAuthenticationFilter`) επικυρώνει tokens
- **Role-Based Access Control**: Spring Security με `@PreAuthorize` annotations
- **Password Encryption**: BCrypt hashing
- **CORS Configuration**: Cross-origin resource sharing για frontend

#### 4.2.4 Database Schema
- **Users**: id, username, email, password, email_verified, verification_token, roles
- **Roles**: id, name (ADMIN, DOCTOR, CITIZEN, SHELTER)
- **Animals**: id, name, type, age, gender, userId, imageUrl, req
- **Requests**: id, name, type, age, gender, userId, imageUrl, AdminApproved, DocApproved

#### 4.2.5 File Upload Implementation
- **MinIO Integration**: 
  - Upload: `FileStorageService.uploadImage()` → MinIO bucket
  - Download: Presigned URLs (valid for 7 days)
  - Delete: `FileStorageService.deleteImage()`
- **File Validation**: Max size 10MB, supported formats: JPEG, PNG, GIF, WebP

#### 4.2.6 Email Service Implementation
- **SMTP Configuration**: Gmail SMTP (smtp.gmail.com:587)
- **Email Types**:
  - Verification email (με token link)
  - Welcome email
  - Login notification
  - New animal notification (με embedded image)
  - Request submitted/approved/rejected emails
- **HTML Emails**: Support για HTML emails με embedded images

#### 4.2.7 ThingsBoard Integration
- **Telemetry Sending**: Scheduled task κάθε 30 δευτερόλεπτα
- **Animal Data**: Όταν προστίθεται νέο ζώο, τα δεδομένα στέλνονται ως telemetry
- **REST API**: HTTP POST requests στο ThingsBoard API

### 4.3 Infrastructure Implementation

#### 4.3.1 Docker Compose
- **Services**: 9 containers (postgres, keycloak, backend, frontend, minio, rabbitmq, thingsboard, node-red, mailhog)
- **Networks**: Bridge network για inter-container communication
- **Volumes**: Persistent storage για databases και MinIO
- **Health Checks**: Health checks για όλα τα services

#### 4.3.2 Kubernetes Deployment
- **Manifests**: Deployment, Service, Ingress, PVC manifests
- **Namespaces**: Isolation με namespace `devops-pets`
- **Storage**: Persistent Volumes για databases
- **Ingress**: Nginx ingress controller για external access

#### 4.3.3 CI/CD (Jenkins)
- **Jenkinsfile**: Pipeline για automated build και deployment
- **Stages**: Build, Test, Docker build, Deploy

### 4.4 Data Flow

#### 4.4.1 User Registration Flow
```
User → Frontend → Backend API → PostgreSQL
                    ↓
                 Email Service → SMTP Server
```

#### 4.4.2 Animal Upload Flow
```
User → Frontend → Backend API → MinIO (image storage)
                    ↓
                 PostgreSQL (metadata)
                    ↓
                 Email Service (notifications)
                    ↓
                 ThingsBoard (telemetry)
```

#### 4.4.3 Authentication Flow
```
User → Frontend → Backend API → Keycloak (OAuth2/OIDC)
                    ↓
                 JWT Token → Frontend (stored in Pinia)
```

#### 4.4.4 IoT Data Flow
```
Backend (Scheduled Task) → ThingsBoard API
                    ↓
                 RabbitMQ (optional)
                    ↓
                 Node-RED (optional processing)
```

### 4.5 Security Measures

#### 4.5.1 Authentication
- JWT tokens με expiration (24 hours)
- Email verification για νέους χρήστες
- Password hashing με BCrypt

#### 4.5.2 Authorization
- Role-based access control (RBAC)
- Endpoint-level security με `@PreAuthorize`
- CORS configuration

#### 4.5.3 Data Protection
- SQL injection prevention (JPA parameterized queries)
- XSS prevention (input validation)
- File upload validation (size, type)

### 4.6 Error Handling

#### 4.6.1 Frontend
- Try-catch blocks για API calls
- Error messages στο UI
- Automatic logout σε 401 errors

#### 4.6.2 Backend
- Global exception handler (`@ExceptionHandler`)
- Validation errors με descriptive messages
- Logging για debugging

### 4.7 Performance Optimizations

#### 4.7.1 Database
- JPA lazy loading για relationships
- Indexes σε frequently queried columns
- Connection pooling

#### 4.7.2 File Storage
- Presigned URLs για direct access (reduces backend load)
- Image compression (future enhancement)

#### 4.7.3 Caching
- JWT token caching (future enhancement)
- Static asset caching (nginx)

## 5. Deployment

### 5.1 Local Development
```bash
docker-compose up -d
```

### 5.2 Kubernetes Production
```bash
kubectl apply -f k8s/
```

### 5.3 Environment Variables
- `GMAIL_USER`: Gmail account για emails
- `GMAIL_PASS`: Gmail app password
- `THINGSBOARD_TOKEN`: ThingsBoard access token
- `FRONTEND_URL`: Frontend URL για email links

## 6. Testing

### 6.1 Unit Tests
- Service layer tests
- Repository tests

### 6.2 Integration Tests
- API endpoint tests
- Database integration tests

### 6.3 E2E Tests
- User registration flow
- Animal upload flow
- Request approval flow

## 7. Future Enhancements

1. **Real-time Notifications**: WebSocket support για real-time updates
2. **Advanced Search**: Full-text search για ζώα
3. **Analytics Dashboard**: Statistics και reports
4. **Mobile App**: React Native ή Flutter mobile application
5. **Payment Integration**: Online payment για adoption fees
6. **Veterinary Records**: Digital health records για ζώα
7. **Social Features**: Comments, ratings, sharing

## 8. Συμπεράσματα

Το Pet Adoption System είναι ένα ολοκληρωμένο, scalable, και secure σύστημα που επιλύει τα προβλήματα διαχείρισης υιοθεσίας κατοικιδίων. Η microservices architecture επιτρέπει εύκολη κλιμάκωση και συντήρηση, ενώ οι σύγχρονες τεχνολογίες (Vue.js, Spring Boot, Docker, Kubernetes) εξασφαλίζουν performance και reliability.

