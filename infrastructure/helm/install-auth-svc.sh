#!/usr/bin/env sh

# Install the auth-svc or upgrade it if it already exists
if ! helm list | grep auth-svc >/dev/null; then
  echo "Installing auth-svc..."
  helm install auth-svc ./auth-svc
else
  echo "Upgrading auth-svc..."
  helm upgrade auth-svc ./auth-svc
fi
