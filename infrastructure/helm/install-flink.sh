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

# Install flink if not already installed or upgrade if already installed
if ! helm list | grep flink >/dev/null; then
  echo "Installing flink..."
  helm install flink oci://registry-1.docker.io/bitnamicharts/flink -f flink/custom-values.yaml
else
  echo "Flink is already installed. Upgrading..."
  helm upgrade flink oci://registry-1.docker.io/bitnamicharts/flink -f flink/custom-values.yaml
fi
