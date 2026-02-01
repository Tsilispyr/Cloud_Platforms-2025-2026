# Quick Fix για RabbitMQ 401 Unauthorized

## Το πρόβλημα
Το RabbitMQ Management UI επιστρέφει 401 Unauthorized ακόμα και με τα σωστά credentials (`rabbitmq` / `rabbitmq123`).

## Αιτία
Το RabbitMQ volume έχει παλιά credentials που δεν ταιριάζουν με τα environment variables. Το RabbitMQ χρησιμοποιεί τα environment variables μόνο κατά την πρώτη αρχικοποίηση.

### Βήμα 1: Clear browser cache

Μετά το script, κάντε:

1. **Firefox**: 
   - Press `Ctrl+Shift+Delete`
   - Select "Cookies" and "Cached Web Content"
   - Time range: "Last Hour"
   - Clear Now

2. **Chrome/Edge**:
   - Press `Ctrl+Shift+Delete`
   - Select "Cookies" and "Cached images and files"
   - Time range: "Last hour"
   - Clear data

3. **Ή χρησιμοποιήστε Incognito/Private mode**:
   - Firefox: `Ctrl+Shift+P`
   - Chrome/Edge: `Ctrl+Shift+N`

### Βήμα 2: Επαλήθευση

1. Ανοίξτε http://localhost:15672 σε incognito mode
2. Χρησιμοποιήστε:
   - **Username**: `rabbitmq`
   - **Password**: `rabbitmq123`

## Αν δεν λειτουργεί

### Ελέγξτε τα logs:
```bash
docker compose -f docker-compose.deploy.yml logs rabbitmq | tail -n 50
```

### Ελέγξτε αν το volume διαγράφηκε:
```bash
docker volume ls | grep rabbitmq
```

Αν βλέπετε volume, διαγράψτε το manually:
```bash
docker volume rm <volume-name>
docker compose -f docker-compose.deploy.yml up -d rabbitmq
```

### Ελέγξτε τα credentials στο container:
```bash
docker exec devops-pets-rabbitmq env | grep RABBITMQ
```

Πρέπει να βλέπετε:
```
RABBITMQ_DEFAULT_USER=rabbitmq
RABBITMQ_DEFAULT_PASS=rabbitmq123
```

## Manual Reset (αν το script δεν λειτουργεί)

```bash
# Stop everything
docker compose -f docker-compose.deploy.yml stop

# Remove RabbitMQ container
docker compose -f docker-compose.deploy.yml rm -f rabbitmq

# Find and remove volume
docker volume ls | grep rabbitmq
docker volume rm <volume-name>

# Start RabbitMQ
docker compose -f docker-compose.deploy.yml up -d rabbitmq

# Wait 15 seconds
sleep 15

# Restart dependent services
docker compose -f docker-compose.deploy.yml up -d thingsboard node-red backend
```

