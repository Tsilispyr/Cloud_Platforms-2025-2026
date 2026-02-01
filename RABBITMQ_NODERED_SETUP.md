# RabbitMQ και Node-RED Setup Guide

## Credentials

### RabbitMQ Management UI
- **URL**: http://localhost:15672
- **Username**: `rabbitmq`
- **Password**: `rabbitmq123`

### Node-RED
- **URL**: http://localhost:1880
- **Volume**: Τα flows αποθηκεύονται στο volume `node-red-data`

## Αν τα Credentials του RabbitMQ δεν λειτουργούν (401 Unauthorized)

Αν έχετε ήδη τρέξει το RabbitMQ πριν, το volume μπορεί να έχει παλιά credentials. Το RabbitMQ χρησιμοποιεί environment variables μόνο κατά την πρώτη αρχικοποίηση.

```bash
# Σταματήστε το RabbitMQ
docker compose -f docker-compose.deploy.yml stop rabbitmq

# Αφαιρέστε το container
docker compose -f docker-compose.deploy.yml rm -f rabbitmq

# Αφαιρέστε το volume (προσοχή: θα χάσετε όλα τα δεδομένα)
docker volume rm devops-pets-frontend-backend-main_rabbitmq-data 2>/dev/null || \
docker volume rm devops-pets-rabbitmq-data 2>/dev/null || \
echo "Volume might not exist"

# Ξεκινήστε ξανά το RabbitMQ
docker compose -f docker-compose.deploy.yml up -d rabbitmq
```

## Επαλήθευση

1. **RabbitMQ Management UI**: Ανοίξτε http://localhost:15672 και συνδεθείτε με `rabbitmq` / `rabbitmq123`
2. **Node-RED**: Ανοίξτε http://localhost:1880 - το warning για το volume θα πρέπει να έχει εξαφανιστεί

## Σύνδεση Node-RED με RabbitMQ

Στο Node-RED, μπορείτε να χρησιμοποιήσετε τα RabbitMQ nodes με:
- **Host**: `rabbitmq` (όνομα του container στο Docker network)
- **Port**: `5672`
- **Username**: `rabbitmq`
- **Password**: `rabbitmq123`
- **Virtual Host**: `/`

## Troubleshooting

### Αν το RabbitMQ δεν ξεκινάει:
```bash
# Ελέγξτε τα logs
docker compose -f docker-compose.deploy.yml logs rabbitmq

# Ελέγξτε αν το container τρέχει
docker ps | grep rabbitmq

# Ελέγξτε το status
docker compose -f docker-compose.deploy.yml ps rabbitmq
```

### Αν λαμβάνετε 401 Unauthorized:
1. **Clear browser cache** - Ο browser μπορεί να έχει cached παλιά credentials
2. **Try incognito/private mode** - Για να αποκλείσετε cache issues
3. **Run fix script** - `./fix-rabbitmq-401.sh` για να reset το volume
4. **Check credentials** - Βεβαιωθείτε ότι χρησιμοποιείτε `rabbitmq` / `rabbitmq123`

### Αν λαμβάνετε 401 Unauthorized:
1. **Clear browser cache** - Ο browser μπορεί να έχει cached παλιά credentials
2. **Try incognito/private mode** - Για να αποκλείσετε cache issues
3. **Run fix script** - `./fix-rabbitmq-401.sh` για να reset το volume
4. **Check credentials** - Βεβαιωθείτε ότι χρησιμοποιείτε `rabbitmq` / `rabbitmq123`

### Αν το Node-RED δεν μπορεί να συνδεθεί στο RabbitMQ:
- Βεβαιωθείτε ότι και τα δύο services είναι στο ίδιο network (`devops-pets-network`)
- Χρησιμοποιήστε το hostname `rabbitmq` (όχι `localhost`) για connections από Node-RED
