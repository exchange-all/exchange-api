#!/usr/bin/env sh

# Install the exchange-svc or upgrade it if it already exists
if ! helm list | grep exchange-svc >/dev/null; then
  echo "Installing exchange-svc..."
  helm install exchange-svc ./exchange-svc
else
  echo "Upgrading exchange-svc..."
  helm upgrade exchange-svc ./exchange-svc
fi
