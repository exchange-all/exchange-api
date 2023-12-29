# K8s Deployment

## Prerequisites

- [Helm](https://helm.sh/docs/intro/install/)
- [Kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
- [Minikube](https://minikube.sigs.k8s.io/docs/start/)
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

## Setup

1. Start minikube

```bash
minikube start
```

2. Add Helm repos

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
```

3. Update Helm repos

```bash
helm repo update
```
