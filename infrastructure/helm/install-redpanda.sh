#!/usr/bin/env sh

# Check helm is installed
if ! command -v helm >/dev/null; then
  echo "Helm is not installed. Please install it first."
  exit 1
fi
# Add redpanda repo if not already added
if ! helm repo list | grep redpanda >/dev/null; then
  helm repo add redpanda https://charts.redpanda.com
  helm repo update
fi

# Add jetstack repo if not already added
if ! helm repo list | grep jetstack >/dev/null; then
  helm repo add jetstack https://charts.jetstack.io
  helm repo update
fi

# Install cert-manager if not already installed
if ! kubectl get ns cert-manager >/dev/null; then
  echo "Installing cert-manager..."
  helm install cert-manager jetstack/cert-manager --set installCRDs=true --namespace cert-manager --create-namespace
fi

# Install redpanda if not already installed or upgrade if already installed
if ! helm list | grep redpanda >/dev/null; then
  echo "Installing redpanda..."
  helm install redpanda redpanda/redpanda -f redpanda/custom-values.yaml
else
  echo "Redpanda is already installed. Upgrading..."
  helm upgrade redpanda redpanda/redpanda -f redpanda/custom-values.yaml
fi
