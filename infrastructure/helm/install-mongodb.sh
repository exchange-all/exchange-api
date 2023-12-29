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

# Install mongodb if not already installed or upgrade if already installed
if ! helm list | grep mongodb >/dev/null; then
  echo "Installing mongodb..."
  helm install mongodb bitnami/mongodb -f mongodb/custom-values.yaml
else
  echo "Mongodb is already installed. Upgrading..."
  helm upgrade mongodb bitnami/mongodb -f mongodb/custom-values.yaml
fi
