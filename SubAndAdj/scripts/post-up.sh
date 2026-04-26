#!/usr/bin/env bash
set -euo pipefail

# post-up.sh
# 안전한 기동 스크립트
# - postgres(및 redis)를 먼저 띄우고, Postgres healthcheck가 'healthy'가 될 때까지 대기
# - 준비되면 전체 서비스를 docker-compose up -d로 기동
# - .env 자동 로드(프로젝트 루트의 .env)

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE_FILE="${COMPOSE_FILE:-$PROJECT_ROOT/docker-compose.yml}"
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-saas-postgres}"
SERVICES_FIRST_START="${SERVICES_FIRST_START:-postgres redis}"
ALL_SERVICES="${ALL_SERVICES:-}" # if empty, docker-compose up -d will start all defined services
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-120}"
INTERVAL_SECONDS="${INTERVAL_SECONDS:-5}"
LOG_DIR="${LOG_DIR:-$PROJECT_ROOT/scripts/logs}"

mkdir -p "$LOG_DIR"
LOG_FILE="$LOG_DIR/post-up_$(date +%F_%H%M%S).log"

# load .env if present
ENV_FILE="$PROJECT_ROOT/.env"
if [ -f "$ENV_FILE" ]; then
  set -a
  # shellcheck disable=SC1090
  . "$ENV_FILE"
  set +a
fi

echo "[$(date +'%F %T')] post-up started" | tee -a "$LOG_FILE"

echo "[$(date +'%F %T')] Starting initial services: $SERVICES_FIRST_START" | tee -a "$LOG_FILE"
docker-compose -f "$COMPOSE_FILE" up -d $SERVICES_FIRST_START >>"$LOG_FILE" 2>&1

start_ts=$(date +%s)
end_ts=$((start_ts + TIMEOUT_SECONDS))

echo "[$(date +'%F %T')] Waiting for Postgres container '$POSTGRES_CONTAINER' to become healthy (timeout ${TIMEOUT_SECONDS}s)" | tee -a "$LOG_FILE"
while true; do
  now=$(date +%s)
  if [ "$now" -ge "$end_ts" ]; then
    echo "[$(date +'%F %T')] ERROR: Timeout waiting for Postgres to become healthy" | tee -a "$LOG_FILE"
    echo "See logs: docker logs $POSTGRES_CONTAINER" | tee -a "$LOG_FILE"
    exit 1
  fi

  # Check if container exists
  if ! docker ps --filter "name=${POSTGRES_CONTAINER}" --format '{{.Names}}' | grep -q "^${POSTGRES_CONTAINER}$"; then
    echo "[$(date +'%F %T')] Postgres container '$POSTGRES_CONTAINER' not found yet. Sleeping ${INTERVAL_SECONDS}s..." | tee -a "$LOG_FILE"
    sleep "$INTERVAL_SECONDS"
    continue
  fi

  # Try to read health status
  health=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "$POSTGRES_CONTAINER" 2>/dev/null || true)
  echo "[$(date +'%F %T')] Current health: $health" | tee -a "$LOG_FILE"

  if [ "$health" = "healthy" ]; then
    echo "[$(date +'%F %T')] Postgres is healthy." | tee -a "$LOG_FILE"
    break
  fi

  sleep "$INTERVAL_SECONDS"
done

echo "[$(date +'%F %T')] Starting remaining services" | tee -a "$LOG_FILE"
if [ -z "$ALL_SERVICES" ]; then
  docker-compose -f "$COMPOSE_FILE" up -d >>"$LOG_FILE" 2>&1
else
  docker-compose -f "$COMPOSE_FILE" up -d $ALL_SERVICES >>"$LOG_FILE" 2>&1
fi

echo "[$(date +'%F %T')] docker-compose up completed" | tee -a "$LOG_FILE"
echo "Log saved to $LOG_FILE"
exit 0

