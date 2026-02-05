# Οδηγίες Deployment - Pet Adoption System

## 1. Προαπαιτούμενα

### 1.1 Απαιτήσεις Συστήματος
- **Docker**: Έκδοση 20.10 ή νεότερη
- **Docker Compose**: Έκδοση 2.0 ή νεότερη
- **Διαθέσιμη Μνήμη**: Τουλάχιστον 4GB RAM
- **Διαθέσιμος Χώρος**: Τουλάχιστον 10GB για volumes
- **Δικτυακή Πρόσβαση**: Για download Docker images

### 1.2 Ελέγχος Εγκατάστασης
``` 
# Έλεγχος Docker
docker --version
 
# Έλεγχος Docker Compose
docker-compose --version

# Έλεγχος διαθέσιμης μνήμης
docker system df
```

## 2. Προετοιμασία Environment Variables

### 2.1 Gmail SMTP Configuration (Προαιρετικό αλλά Συνιστάται)

Για να λειτουργήσει η αποστολή emails (verification, notifications), ρυθμίστε:

**Linux/Mac:**
``` 
export GMAIL_USER="your_email@gmail.com"
export GMAIL_PASS="your_16_digit_app_password"
```

**Windows PowerShell:**
```powershell
$env:GMAIL_USER="your_email@gmail.com"
$env:GMAIL_PASS="your_16_digit_app_password"
```

**Windows CMD:**
```cmd
set GMAIL_USER=your_email@gmail.com
set GMAIL_PASS=your_16_digit_app_password
```

> **Σημείωση**: Χρειάζεστε **Gmail App Password** (όχι το κανονικό password).
> 1. Google Account → Security → 2-Step Verification → App passwords
> 2. Δημιουργήστε νέο για "Mail"
> 3. Αντιγράψτε το 16-ψήφιο password

### 2.2 ThingsBoard Token (Προαιρετικό)

Για να λειτουργήσει η ενσωμάτωση με ThingsBoard:

``` 
export THINGSBOARD_TOKEN="your_access_token_here"
```

**Πώς να λάβετε το Token:**
1. Μετά το deployment, ανοίξτε το ThingsBoard: http://localhost:9090
2. Login με: `sysadmin@thingsboard.org` / `sysadmin`
3. Δημιουργήστε ένα Device (π.χ. "Pet Device")
4. Αντιγράψτε το **Access Token**
5. Ορίστε το ως environment variable και επανεκκινήστε το backend

## 3. Deployment Steps

### 3.1 Clone/Download Project
``` 
# Αν έχετε το project σε git repository
git clone <repository-url>
cd Cloud_Platforms-2025-2026-main

# Ή μεταβείτε στον φάκελο του project
cd /path/to/Cloud_Platforms-2025-2026-main
```

### 3.2 Ελέγχος Ports

Βεβαιωθείτε ότι τα παρακάτω ports είναι διαθέσιμα:
- **5432**: PostgreSQL (main)
- **5432**: PostgreSQL (ThingsBoard) - internal only
- **8080**: Backend API
- **8081**: Keycloak
- **8083**: Frontend
- **9000**: MinIO API
- **9001**: MinIO Console
- **5672**: RabbitMQ AMQP
- **15672**: RabbitMQ Management UI
- **1880**: Node-RED
- **9090**: ThingsBoard
- **8025**: MailHog Web UI
- **1025**: MailHog SMTP

**Έλεγχος αν ports είναι σε χρήση:**
``` 
# Linux/Mac
netstat -tuln | grep -E ':(5432|8080|8081|8083|9000|9001|5672|15672|1880|9090|8025)'

# Windows
netstat -ano | findstr "5432 8080 8081 8083 9000 9001 5672 15672 1880 9090 8025"
```

### 3.3 Build και Start Services

``` 
# Build και start όλα τα services
docker-compose -f docker-compose.deploy.yml up -d --build

# Ή για να δείτε τα logs σε real-time
docker-compose -f docker-compose.deploy.yml up --build
```

### 3.4 Έλεγχος Status

``` 
# Έλεγχος status όλων των containers
docker-compose -f docker-compose.deploy.yml ps

# Έλεγχος logs για συγκεκριμένο service
docker-compose -f docker-compose.deploy.yml logs backend
docker-compose -f docker-compose.deploy.yml logs frontend

# Έλεγχος όλων των logs
docker-compose -f docker-compose.deploy.yml logs -f
```

## 4. Services και Access URLs

### 4.1 Application Services

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:8083 | Web Interface |
| **Backend API** | http://localhost:8080 | REST API |
| **Backend Health** | http://localhost:8080/actuator/health | Health Check |
| **Backend Test** | http://localhost:8080/api/test | Test Endpoint |

### 4.2 Infrastructure Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **Keycloak** | http://localhost:8081 | admin / admin |
| **MinIO Console** | http://localhost:9001 | minioadmin / minioadmin123 |
| **MinIO API** | http://localhost:9000 | minioadmin / minioadmin123 |
| **RabbitMQ Management** | http://localhost:15672 | rabbitmq / rabbitmq123 |
| **ThingsBoard** | http://localhost:9090 | sysadmin@thingsboard.org / sysadmin |
| **Node-RED** | http://localhost:1880 | - |
| **MailHog Web UI** | http://localhost:8025 | - |

### 4.3 Database Connections

| Database | Host | Port | Database | User | Password |
|----------|------|------|----------|------|----------|
| **PostgreSQL (Main)** | localhost | 5432 | petdb | petuser | petpass |
| **PostgreSQL (ThingsBoard)** | localhost | 5432 | thingsboard | thingsboard | thingsboard |

## 5. Service Dependencies και Startup Order

Το Docker Compose χειρίζεται αυτόματα τις dependencies, αλλά η σειρά startup είναι:

1. **PostgreSQL** (main) - Health check: `pg_isready`
2. **PostgreSQL** (ThingsBoard) - Health check: `pg_isready`
3. **Keycloak** - Depends on: PostgreSQL (main)
4. **MinIO** - Health check: `curl /minio/health/live`
5. **RabbitMQ** - Health check: `rabbitmq-diagnostics ping`
6. **PostgreSQL-TB** - Health check: `pg_isready`
7. **ThingsBoard** - Depends on: RabbitMQ, PostgreSQL-TB
8. **Backend** - Depends on: PostgreSQL, MinIO, ThingsBoard
9. **Node-RED** - Depends on: RabbitMQ, ThingsBoard
10. **Frontend** - Depends on: Backend
11. **MailHog** - Standalone

## 6. Αρχική Ρύθμιση

### 6.1 Keycloak Setup

1. Ανοίξτε: http://localhost:8081
2. Login ως admin: `admin` / `admin`
3. Δημιουργήστε Realm: `pet-realm`
4. Δημιουργήστε Client:
   - Client ID: `pet-client`
   - Client Protocol: `openid-connect`
   - Access Type: `public` ή `confidential`
5. Ρυθμίστε Redirect URIs:
   - `http://localhost:8083/*`
   - `http://localhost:8080/*`

### 6.2 MinIO Setup

1. Ανοίξτε: http://localhost:9001
2. Login: `minioadmin` / `minioadmin123`
3. Δημιουργήστε Bucket: `pets-images`
4. (Προαιρετικό) Δημιουργήστε Bucket: `pets-images-archive`
5. Ρυθμίστε Access Policy αν χρειάζεται

### 6.3 ThingsBoard Setup

1. Ανοίξτε: http://localhost:9090
2. Login: `sysadmin@thingsboard.org` / `sysadmin`
3. Δημιουργήστε Device (π.χ. "Pet Device")
4. Αντιγράψτε το Access Token
5. Ορίστε το ως environment variable:
   ``` 
   export THINGSBOARD_TOKEN="your_token_here"
   ```
6. Επανεκκινήστε το backend:
   ``` 
   docker-compose -f docker-compose.deploy.yml restart backend
   ```

### 6.4 Backend Configuration

Το Backend διαβάζει τις παρακάτω environment variables:

``` 
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/petdb
SPRING_DATASOURCE_USERNAME=petuser
SPRING_DATASOURCE_PASSWORD=petpass

# MinIO
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123
MINIO_BUCKET=pets-images

# Email (Gmail)
GMAIL_USER=${GMAIL_USER:-}
GMAIL_PASS=${GMAIL_PASS:-}
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587

# Frontend
FRONTEND_URL=http://localhost:8083

# ThingsBoard
THINGSBOARD_URL=http://thingsboard:8080
THINGSBOARD_TOKEN=${THINGSBOARD_TOKEN:-}

# RabbitMQ
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=rabbitmq
SPRING_RABBITMQ_PASSWORD=rabbitmq123
```

## 7. Έλεγχος Deployment

### 7.1 Health Checks

``` 
# Έλεγχος όλων των services
docker-compose -f docker-compose.deploy.yml ps

# Έλεγχος health status
docker-compose -f docker-compose.deploy.yml ps | grep -E "(healthy|unhealthy)"

# Manual health check για backend
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/test

# Manual health check για frontend
curl http://localhost:8083
```

### 7.2 Logs Inspection

``` 
# Backend logs
docker-compose -f docker-compose.deploy.yml logs backend | tail -50

# Frontend logs
docker-compose -f docker-compose.deploy.yml logs frontend | tail -50

# Database logs
docker-compose -f docker-compose.deploy.yml logs postgres | tail -50

# All services logs
docker-compose -f docker-compose.deploy.yml logs --tail=100
```

### 7.3 Database Connection Test

``` 
# Connect to PostgreSQL (main)
docker exec -it devops-pets-postgres psql -U petuser -d petdb

# Connect to PostgreSQL (ThingsBoard)
docker exec -it devops-pets-postgres-tb psql -U thingsboard -d thingsboard

# Test queries
# \dt  # List tables
# SELECT * FROM users;
# \q   # Exit
```
#### Έλεγχος RabbitMQ queue για message publishment (το ui δεν τα δείχνει πάντα, μάλλον το backend τα επεξεργάζεται γρήγορα και πέφτουν ανάμεσα στα ping του RabbitMQ που έχουν ανά 5 δευτερόλεπτα ανανέωση)
   ```
   docker exec devops-pets-rabbitmq rabbitmqadmin -u rabbitmq -p rabbitmq123 list queues name message_stats.publish message_stats.deliver_get
```
## 8. Troubleshooting

### 8.1 Backend δεν ξεκινάει

**Πρόβλημα**: Backend container crash loop

**Λύσεις**:
``` 
# Έλεγχος logs
docker-compose -f docker-compose.deploy.yml logs backend

# Έλεγχος αν το PostgreSQL είναι ready
docker exec devops-pets-postgres pg_isready -U petuser -d petdb

# Έλεγχος αν το MinIO είναι ready
curl http://localhost:9000/minio/health/live

# Restart backend
docker-compose -f docker-compose.deploy.yml restart backend
```

### 8.2 Frontend δεν φορτώνει

**Πρόβλημα**: Frontend returns 502 Bad Gateway

**Λύσεις**:
``` 
# Έλεγχος αν το backend είναι running
curl http://localhost:8080/api/test

# Έλεγχος frontend logs
docker-compose -f docker-compose.deploy.yml logs frontend

# Restart frontend
docker-compose -f docker-compose.deploy.yml restart frontend
```

### 8.3 Database Connection Errors

**Πρόβλημα**: `Connection refused` ή `Connection timeout`

**Λύσεις**:
``` 
# Έλεγχος αν το PostgreSQL είναι running
docker-compose -f docker-compose.deploy.yml ps postgres

# Έλεγχος network connectivity
docker exec devops-pets-backend ping postgres

# Restart PostgreSQL
docker-compose -f docker-compose.deploy.yml restart postgres
```

### 8.4 MinIO Upload Failures

**Πρόβλημα**: Cannot upload images

**Λύσεις**:
``` 
# Έλεγχος MinIO status
curl http://localhost:9000/minio/health/live

# Έλεγχος αν το bucket υπάρχει
docker exec devops-pets-minio mc ls minio/

# Δημιουργία bucket αν λείπει
docker exec devops-pets-minio mc mb minio/pets-images
```

### 8.5 Email δεν στέλνεται

**Πρόβλημα**: Emails δεν φτάνουν

**Λύσεις**:
``` 
# Έλεγχος MailHog (για development)
curl http://localhost:8025

# Έλεγχος Gmail credentials
echo $GMAIL_USER
echo $GMAIL_PASS

# Έλεγχος backend logs για email errors
docker-compose -f docker-compose.deploy.yml logs backend | grep -i email
```

### 8.6 ThingsBoard Integration Issues

**Πρόβλημα**: ThingsBoard δεν λαμβάνει telemetry

**Λύσεις**:
``` 
# Έλεγχος ThingsBoard status
curl http://localhost:9090/api/v1/health

# Έλεγχος RabbitMQ connection
docker exec devops-pets-rabbitmq rabbitmq-diagnostics ping

# Έλεγχος token
echo $THINGSBOARD_TOKEN

# Restart backend μετά από token update
docker-compose -f docker-compose.deploy.yml restart backend
```

## 9. Maintenance Commands

### 9.1 Stop Services

``` 
# Stop όλα τα services
docker-compose -f docker-compose.deploy.yml stop

# Stop συγκεκριμένο service
docker-compose -f docker-compose.deploy.yml stop backend
```

### 9.2 Start Services

``` 
# Start όλα τα services
docker-compose -f docker-compose.deploy.yml start

# Start συγκεκριμένο service
docker-compose -f docker-compose.deploy.yml start backend
```

### 9.3 Restart Services

``` 
# Restart όλα τα services
docker-compose -f docker-compose.deploy.yml restart

# Restart συγκεκριμένο service
docker-compose -f docker-compose.deploy.yml restart backend
```

### 9.4 Rebuild Services

``` 
# Rebuild όλα τα services
docker-compose -f docker-compose.deploy.yml up -d --build

# Rebuild συγκεκριμένο service
docker-compose -f docker-compose.deploy.yml up -d --build backend
```

### 9.5 Remove Services

``` 
# Stop και remove containers (χωρίς volumes)
docker-compose -f docker-compose.deploy.yml down

# Stop και remove containers με volumes (WARNING: Διαγράφει δεδομένα!)
docker-compose -f docker-compose.deploy.yml down -v
```

### 9.6 Cleanup

``` 
# Remove stopped containers
docker-compose -f docker-compose.deploy.yml rm

# Remove unused images
docker image prune -a

# Remove unused volumes (ΠΡΟΣΟΧΗ: Διαγράφει δεδομένα!)
docker volume prune
```

## 10. Backup και Restore

### 10.1 Database Backup

``` 
# Backup PostgreSQL (main)
docker exec devops-pets-postgres pg_dump -U petuser petdb > backup_petdb_$(date +%Y%m%d).sql

# Backup PostgreSQL (ThingsBoard)
docker exec devops-pets-postgres-tb pg_dump -U thingsboard thingsboard > backup_thingsboard_$(date +%Y%m%d).sql
```

### 10.2 Database Restore

``` 
# Restore PostgreSQL (main)
cat backup_petdb_20240101.sql | docker exec -i devops-pets-postgres psql -U petuser petdb

# Restore PostgreSQL (ThingsBoard)
cat backup_thingsboard_20240101.sql | docker exec -i devops-pets-postgres-tb psql -U thingsboard thingsboard
```

### 10.3 MinIO Backup

``` 
# Backup MinIO data
docker run --rm -v devops-pets_minio-data:/data -v $(pwd):/backup alpine tar czf /backup/minio_backup_$(date +%Y%m%d).tar.gz /data
```

### 10.4 MinIO Restore

``` 
# Restore MinIO data
docker run --rm -v devops-pets_minio-data:/data -v $(pwd):/backup alpine tar xzf /backup/minio_backup_20240101.tar.gz -C /
```

## 11. Production Considerations

### 11.1 Security

⚠️ **ΣΗΜΑΝΤΙΚΟ**: Για production deployment:

1. **Αλλαγή Passwords**:
   - PostgreSQL passwords
   - MinIO credentials
   - RabbitMQ credentials
   - Keycloak admin password

2. **Environment Variables**:
   - Χρησιμοποιήστε `.env` file ή secrets management
   - Μην hardcode passwords στον κώδικα

3. **Network Security**:
   - Χρησιμοποιήστε reverse proxy (nginx/traefik)
   - Enable HTTPS/TLS
   - Restrict access με firewall rules

4. **Keycloak**:
   - Configure proper realm settings
   - Enable 2FA
   - Set token expiration times

### 11.2 Performance

1. **Resource Limits**:
   ```yaml
   deploy:
     resources:
       limits:
         cpus: '2'
         memory: 2G
   ```

2. **Database Optimization**:
   - Configure connection pooling
   - Add indexes
   - Regular VACUUM

3. **Caching**:
   - Redis για session management
   - CDN για static assets

### 11.3 Monitoring

1. **Health Checks**: Όλα τα services έχουν health checks
2. **Logging**: Centralized logging με ELK stack
3. **Metrics**: Prometheus + Grafana
4. **Alerting**: Configure alerts για critical services

## 12. Network Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Network                           │
│              (devops-pets-network)                          │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │ Frontend │  │ Backend  │  │ Keycloak │  │ MinIO    │     │
│  │ :8083    │  │ :8080    │  │ :8081    │  │ :9000    │     │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘     │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │PostgreSQL│  │ RabbitMQ │  │ThingsBoard│ │ Node-RED │     │
│  │ :5432    │  │ :5672    │  │ :9090    │  │ :1880    │     │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘     │
│                                                             │
│  ┌──────────┐                                               │
│  │ MailHog  │                                               │
│  │ :8025    │                                               │
│  └──────────┘                                               │
└─────────────────────────────────────────────────────────────┘
```

## 13. Quick Reference

### 13.1 Common Commands

``` 
# Start all services
docker-compose -f docker-compose.deploy.yml up -d

# Stop all services
docker-compose -f docker-compose.deploy.yml stop

# View logs
docker-compose -f docker-compose.deploy.yml logs -f

# Restart specific service
docker-compose -f docker-compose.deploy.yml restart backend

# Check status
docker-compose -f docker-compose.deploy.yml ps

# Remove everything
docker-compose -f docker-compose.deploy.yml down -v
```

### 13.2 Access URLs

- Frontend: http://localhost:8083
- Backend: http://localhost:8080
- Keycloak: http://localhost:8081
- MinIO Console: http://localhost:9001
- RabbitMQ: http://localhost:15672
- ThingsBoard: http://localhost:9090
- Node-RED: http://localhost:1880
- MailHog: http://localhost:8025

## 14. Support και Documentation

- **Backend Documentation**: `BACKEND.md`
- **Frontend Documentation**: `FRONTEND.md`
- **Design Document**: `DESIGN.md` ή `ΤΕΥΧΟΣ_ΣΧΕΔΙΑΣΜΟΥ.md`
- **Architecture Document**: `ΑΡΧΙΤΕΚΤΟΝΙΚΗ_ΣΥΣΤΗΜΑΤΟΣ.md`
- **Kubernetes Deployment**: `K8S.md`
- **Troubleshooting**: `TROUBLESHOOTING_502.md`, `QUICK_FIX_401.md`

## 15. Συμπεράσματα

Το deployment script (`docker-compose.deploy.yml`) διαχειρίζεται:
-  8 services (PostgreSQL x2, Keycloak, MinIO, Backend, Frontend, RabbitMq, Node-RED, MailHog)
-  Health checks για όλα τα services
-  Service dependencies και startup order
-  Network isolation
-  Persistent volumes για data
-  Environment variable configuration

Ακολουθήστε τα βήματα παραπάνω για successful deployment!





