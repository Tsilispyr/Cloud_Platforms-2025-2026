# Kubernetes Deployment

Οδηγίες για deployment του συστήματος σε Kubernetes cluster.

## Prerequisites
- Kubernetes cluster (Minikube, Kind, κλπ).
- `kubectl` command line tool.

## Configuration Files
Τα αρχεία configuration βρίσκονται στον φάκελο `k8s/`.

### Storage
Το σύστημα χρησιμοποιεί Persistent Volumes για την αποθήκευση δεδομένων.
Δείτε το `k8s/shared-storage.yaml` για τον ορισμό των PV και PVC.

## Deployment Steps

1. **Δημιουργία Namespace**:
   ```bash
   kubectl create namespace devops-pets
   ```

2. **Εφαρμογή Configurations**:
   ```bash
   kubectl apply -f k8s/
   ```

3. **Έλεγχος Status**:
   ```bash
   kubectl get pods -n devops-pets
   ```