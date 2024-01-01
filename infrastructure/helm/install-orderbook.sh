#!/usr/bin/env sh

# Install the orderbook-svc or upgrade it if it already exists
if ! helm list | grep orderbook-svc >/dev/null; then
  echo "Installing orderbook-svc..."
  helm install orderbook-svc ./orderbook-svc
else
  echo "Upgrading orderbook-svc..."
  helm upgrade orderbook-svc ./orderbook-svc
fi
