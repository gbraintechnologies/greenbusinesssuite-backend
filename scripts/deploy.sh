#!/usr/bin/env bash
# Remote deploy for the Green Business Suite backend.
# Mirrors the frontend/autobus-web flow: pull, ensure Docker network, build, up.
set -euo pipefail

BRANCH="${DEPLOY_BRANCH:-main}"
BUILD_NO_CACHE="${BUILD_NO_CACHE:-false}"
PULL_IMAGES="${PULL_IMAGES:-true}"
NEEDS_GBS_NET="${NEEDS_GBS_NET:-true}"
HEALTH_RETRIES="${HEALTH_RETRIES:-36}"

if [ -f docker-compose.yaml ]; then
  COMPOSE_FILE="docker-compose.yaml"
elif [ -f docker-compose.yml ]; then
  COMPOSE_FILE="docker-compose.yml"
else
  echo "No docker-compose file found in $(pwd)" >&2
  exit 1
fi

compose() {
  docker compose -f "$COMPOSE_FILE" "$@"
}

ensure_network() {
  local name="$1"
  if ! docker network inspect "$name" >/dev/null 2>&1; then
    docker network create "$name"
    echo "Created network '$name'"
  fi
}

set_env() {
  local key="$1"
  local value="$2"
  if [ -z "$value" ]; then
    return 0
  fi
  if grep -q "^${key}=" .env; then
    sed -i "s|^${key}=.*|${key}=${value}|" .env
  else
    echo "${key}=${value}" >> .env
  fi
}

if [ "$NEEDS_GBS_NET" = "true" ]; then
  ensure_network greenbusinesssuite
fi

git fetch origin "$BRANCH"
git reset --hard "origin/$BRANCH"

touch .env
set_env AWS_REGION "${AWS_REGION:-us-east-1}"
set_env AWS_BUCKET_NAME "${AWS_BUCKET_NAME:-mbs}"
set_env AWS_ENDPOINT "${AWS_ENDPOINT:-https://usc1.contabostorage.com}"
set_env AWS_ACCESS_KEY_ID "${AWS_ACCESS_KEY_ID:-}"
set_env AWS_SECRET_ACCESS_KEY "${AWS_SECRET_ACCESS_KEY:-}"
if ! grep -q '^AWS_ACCESS_KEY_ID=.\+' .env || ! grep -q '^AWS_SECRET_ACCESS_KEY=.\+' .env; then
  echo "ERROR: Contabo credentials missing in .env (AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY)"
  exit 1
fi

if [ "$PULL_IMAGES" = "true" ]; then
  compose pull --ignore-buildable 2>/dev/null || true
fi

BUILD_ARGS=()
if [ "$BUILD_NO_CACHE" = "true" ]; then
  BUILD_ARGS+=(--no-cache)
fi

compose build "${BUILD_ARGS[@]}"
compose up -d --remove-orphans
docker image prune -f

echo "Waiting for backend health..."
for i in $(seq 1 "$HEALTH_RETRIES"); do
  if docker exec gbs_backend curl -fsS http://localhost:8081/actuator/health >/dev/null 2>&1 \
    || docker exec gbs_backend curl -fsS http://localhost:8081/health >/dev/null 2>&1; then
    echo "Backend healthy"
    compose ps
    exit 0
  fi
  sleep 5
done

echo "Backend failed health check"
compose logs --tail=100 backend
exit 1
