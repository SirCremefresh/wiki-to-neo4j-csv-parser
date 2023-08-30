#!/usr/bin/env sh
set -e

echo "Starting Neo4j..."
docker compose up --wait --detach
