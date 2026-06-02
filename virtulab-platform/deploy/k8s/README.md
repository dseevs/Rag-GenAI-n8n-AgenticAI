# Kubernetes (optional minikube)

```bash
# Build images locally (example)
docker build -f deploy/docker/Dockerfile --build-arg SERVICE=auth-service -t ghcr.io/virtulab/auth-service:latest .

# minikube
minikube start
kubectl apply -f deploy/k8s/namespace.yaml
kubectl apply -f deploy/k8s/configmap.yaml
kubectl apply -f deploy/k8s/secret.yaml
kubectl apply -f deploy/k8s/auth-service.yaml
kubectl apply -f deploy/k8s/graphql-gateway.yaml
kubectl get pods -n virtulab
```

Replace `ghcr.io/virtulab/*` with your registry after CI publish.

For local infra (Postgres, Redis, Kafka) use Docker Compose alongside minikube, or add Helm charts later.
