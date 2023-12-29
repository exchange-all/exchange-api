#!/usr/bin/env sh

# Check helm is installed
if ! command -v helm >/dev/null; then
  echo "Helm is not installed. Please install it first."
  exit 1
fi

# Add timescale repo if not already added
if ! helm repo list | grep timescale >/dev/null; then
  helm repo add timescale 'https://charts.timescale.com/'
  helm repo update
fi

# Install timescaledb if not already installed or upgrade if already installed
if ! helm list | grep timescaledb >/dev/null; then
  echo "Installing timescaledb..."
  helm install timescaledb timescale/timescaledb-single -f timescaledb/custom-values.yaml
else
  echo "Timescaledb is already installed. Upgrading..."
  helm upgrade timescaledb timescale/timescaledb-single -f timescaledb/custom-values.yaml
fi
