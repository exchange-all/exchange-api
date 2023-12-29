#!/usr/bin/env sh

# Check helm is installed
if ! command -v helm >/dev/null; then
  echo "Helm is not installed. Please install it first."
  exit 1
fi

# Add bitnami repo if not already added
if ! helm repo list | grep bitnami >/dev/null; then
  helm repo add bitnami https://charts.bitnami.com/bitnami
  helm repo update
fi

# Install redis if not already installed or upgrade if already installed
if ! helm list | grep redis >/dev/null; then
  echo "Installing redis..."
  helm install redis bitnami/redis -f redis/custom-values.yaml
else
  echo "Redis is already installed. Upgrading..."
  helm upgrade redis bitnami/redis -f redis/custom-values.yaml
fi
