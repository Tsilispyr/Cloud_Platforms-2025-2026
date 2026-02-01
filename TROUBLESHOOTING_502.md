# Troubleshooting 502 Bad Gateway Error

## Το πρόβλημα
Μετά από διαγραφή volumes, το frontend λαμβάνει 502 Bad Gateway errors όταν προσπαθεί να συνδεθεί στο backend.

## Αιτίες
1. **Backend δεν έχει ξεκινήσει** - Το backend χρειάζεται χρόνο να αρχικοποιηθεί
2. **Backend crash** - Το backend δεν μπορεί να συνδεθεί στο database ή άλλα dependencies
3. **Network issues** - Το backend δεν είναι στο ίδιο network με τα dependencies
4. **Database initialization** - Το PostgreSQL χρειάζεται χρόνο να αρχικοποιηθεί μετά από διαγραφή volume

## Λύσεις

### 1. Restart όλα τα services με τη σωστή σειρά

**Bash Script:**
```bash
chmod +x restart-after-volume-delete.sh
./restart-after-volume-delete.sh
```

**Manual:**
```bash
# Σταματήστε όλα
docker-compose -f docker-compose.deploy.yml stop

# Αφαιρέστε containers
docker-compose -f docker-compose.deploy.yml rm -f

# Ξεκινήστε infrastructure πρώτα
docker-compose -f docker-compose.deploy.yml up -d postgres postgres-tb minio rabbitmq

# Περιμένετε 20-30 δευτερόλεπτα
sleep 30

# Ξεκινήστε ThingsBoard
docker-compose -f docker-compose.deploy.yml up -d thingsboard

# Περιμένετε 15 δευτερόλεπτα
sleep 15

# Ξεκινήστε backend
docker-compose -f docker-compose.deploy.yml up -d backend

# Περιμένετε 30-60 δευτερόλεπτα για το backend να αρχικοποιηθεί
sleep 60

# Ξεκινήστε frontend και άλλα
docker-compose -f docker-compose.deploy.yml up -d frontend node-red keycloak mailhog
```

### 2. Ελέγξτε τα logs του backend

```bash
# Δείτε τα logs του backend
docker-compose -f docker-compose.deploy.yml logs backend

# Follow τα logs σε real-time
docker-compose -f docker-compose.deploy.yml logs -f backend
```

**Τι να ψάχνετε:**
- Database connection errors
- "Started Application" message (σημαίνει ότι το backend ξεκίνησε)
- Port binding errors
- Network connection errors

### 3. Ελέγξτε αν το backend τρέχει

```bash
# Δείτε status όλων των containers
docker-compose -f docker-compose.deploy.yml ps

# Ελέγξτε αν το backend container τρέχει
docker ps | grep backend

# Test αν το backend απαντάει
curl http://localhost:8080/api
```

### 4. Ελέγξτε database connection

```bash
# Test PostgreSQL connection
docker exec devops-pets-postgres psql -U petuser -d petdb -c "SELECT 1;"

# Ελέγξτε αν το PostgreSQL είναι healthy
docker-compose -f docker-compose.deploy.yml ps postgres
```

### 5. Ελέγξτε network connectivity

```bash
# Ελέγξτε αν το backend μπορεί να φτάσει στο postgres
docker exec devops-pets-backend ping -c 3 postgres

# Ελέγξτε αν το backend μπορεί να φτάσει στο minio
docker exec devops-pets-backend ping -c 3 minio
```

### 6. Αν το backend crash-αρε

```bash
# Δείτε γιατί crash-αρε
docker-compose -f docker-compose.deploy.yml logs backend | grep -E "ERROR|Exception|Failed"

# Restart μόνο το backend
docker-compose -f docker-compose.deploy.yml restart backend

# Αν δεν λειτουργεί, rebuild
docker-compose -f docker-compose.deploy.yml up -d --build backend
```

### 7. Database initialization issues

Αν το database volume διαγράφηκε, το PostgreSQL θα δημιουργήσει νέο database. Το backend χρειάζεται:
- `SPRING_JPA_HIBERNATE_DDL_AUTO: update` για να δημιουργήσει τα tables
- Χρόνο να ολοκληρώσει την αρχικοποίηση (30-90 δευτερόλεπτα)

### 8. CORS Issues

Αν βλέπετε CORS errors στο browser console:
- Το backend έχει CORS enabled στο SecurityConfig
- Βεβαιωθείτε ότι το `FRONTEND_URL` environment variable είναι σωστό
- Το CORS configuration επιτρέπει όλες τις origins (`*`)

## Προληπτικά μέτρα

1. **Πάντα ξεκινάτε infrastructure services πρώτα** (postgres, minio, rabbitmq)
2. **Περιμένετε να γίνουν healthy** πριν ξεκινήσετε dependent services
3. **Χρησιμοποιήστε healthchecks** - τα services περιμένουν τα dependencies να είναι healthy
4. **Ελέγξτε τα logs** αν κάτι δεν λειτουργεί

## Quick Fix Script

Αν τίποτα δεν λειτουργεί, τρέξτε:

```bash
# Complete reset
docker-compose -f docker-compose.deploy.yml down -v
docker-compose -f docker-compose.deploy.yml up -d

# Περιμένετε 2-3 λεπτά και ελέγξτε
sleep 180
docker-compose -f docker-compose.deploy.yml ps
docker-compose -f docker-compose.deploy.yml logs backend | tail -n 50
```
