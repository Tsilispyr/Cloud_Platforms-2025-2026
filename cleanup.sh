#!/bin/bash

# Script to clean up all containers related to the project to fix name conflicts

echo "Stopping and removing containers..."

# Try standard compose down first
docker compose -f docker-compose.deploy.yml down --remove-orphans 2>/dev/null || true

# Force remove specific containers that might be causing conflicts
CONTAINERS=(
  "devops-pets-rabbitmq"
  "devops-pets-mailhog"
  "devops-pets-postgres"
  "devops-pets-postgres-tb"
  "devops-pets-keycloak"
  "devops-pets-minio"
  "devops-pets-backend"
  "devops-pets-frontend"
  "devops-pets-node-red"
  "thingsboard"
)

for container in "${CONTAINERS[@]}"; do
    echo "Force removing ${container}..."
    docker rm -f "${container}" 2>/dev/null || true
done

echo "Cleanup complete. You can now run 'docker compose -f docker-compose.deploy.yml up -d --build'"