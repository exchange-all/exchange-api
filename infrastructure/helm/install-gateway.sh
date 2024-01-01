#!/usr/bin/env sh

# Install the gateway-svc or upgrade it if it already exists
if ! helm list | grep gateway-svc >/dev/null; then
  echo "Installing gateway-svc..."
  helm install gateway-svc ./gateway-svc
else
  echo "Upgrading gateway-svc..."
  helm upgrade gateway-svc ./gateway-svc
fi
