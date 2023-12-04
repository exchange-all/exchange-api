#!/bin/zsh
# This script is used to start the development environment
docker-compose -f docker-compose.dev.yaml down --remove-orphans --volumes
docker-compose -f docker-compose.dev.yaml up -d --build
