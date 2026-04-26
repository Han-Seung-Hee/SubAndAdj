#!/usr/bin/env bash
set -euo pipefail

# pre-down.sh
# 통합 백업 + 안전한 docker-compose down 래퍼 스크립트
# - 프로젝트 루트에서 실행을 가정
# - 기본: 컨테이너 이름 saas-postgres, DB 이름 subAndAdj, DB user xorhd1222
# - 환경변수로 오버라이드 가능: DB_PASSWORD, POSTGRES_CONTAINER, DB_NAME, DB_USER, COMPOSE_FILE, BACKUP_DIR, FORCE

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE_FILE="${COMPOSE_FILE:-$PROJECT_ROOT/docker-compose.yml}"
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-saas-postgres}"
DB_NAME="${DB_NAME:-subAndAdj}"
DB_USER="${DB_USER:-xorhd1222}"
BACKUP_DIR="${BACKUP_DIR:-$PROJECT_ROOT/backups}"
LOG_DIR="${LOG_DIR:-$PROJECT_ROOT/scripts/logs}"
FORCE="${FORCE:-false}"

mkdir -p "$BACKUP_DIR" "$LOG_DIR"
LOG_FILE="$LOG_DIR/pre-down_$(date +%F_%H%M%S).log"

# If a .env file exists in project root, load it and export variables
# This allows keeping secrets/config in .env without manually exporting them.
ENV_FILE="$PROJECT_ROOT/.env"
if [ -f "$ENV_FILE" ]; then
  # Export variables from .env (works for KEY=VALUE lines)
  set -a
  # shellcheck disable=SC1090
  . "$ENV_FILE"
  set +a
fi

echo "[$(date +'%F %T')] pre-down started" | tee -a "$LOG_FILE"

if [ -z "${DB_PASSWORD:-}" ] && [ "$FORCE" != "true" ]; then
  echo "[$(date +'%F %T')] ERROR: DB_PASSWORD not set. Set DB_PASSWORD env var or run with FORCE=true to skip password check." | tee -a "$LOG_FILE"
  exit 1
fi

OUT_FILE="$BACKUP_DIR/${DB_NAME}_$(date +%F_%H%M%S).sql.gz"

echo "[$(date +'%F %T')] Backing up DB '$DB_NAME' to $OUT_FILE" | tee -a "$LOG_FILE"

# If container is running, use docker exec to run pg_dump inside container (streamed to host)
if docker ps --filter "name=${POSTGRES_CONTAINER}" --format '{{.Names}}' | grep -q "^${POSTGRES_CONTAINER}$"; then
  echo "[$(date +'%F %T')] Detected running container: $POSTGRES_CONTAINER -> using docker exec pg_dump" | tee -a "$LOG_FILE"
  # run pg_dump inside container; pass PGPASSWORD into the container process
  if docker exec -e PGPASSWORD="$DB_PASSWORD" -t "$POSTGRES_CONTAINER" pg_dump -U "$DB_USER" -d "$DB_NAME" --no-owner --no-acl | gzip > "$OUT_FILE" 2>>"$LOG_FILE"; then
    echo "[$(date +'%F %T')] Backup succeeded: $OUT_FILE" | tee -a "$LOG_FILE"
  else
    echo "[$(date +'%F %T')] ERROR: Backup inside container failed." | tee -a "$LOG_FILE"
    if [ "$FORCE" = "true" ]; then
      echo "[$(date +'%F %T')] FORCE=true: continuing to docker-compose down despite backup failure." | tee -a "$LOG_FILE"
    else
      echo "[$(date +'%F %T')] Aborting. Set FORCE=true to skip backup failures (not recommended)." | tee -a "$LOG_FILE"
      exit 2
    fi
  fi
else
  echo "[$(date +'%F %T')] Container $POSTGRES_CONTAINER not running; attempting host pg_dump (localhost:5433)" | tee -a "$LOG_FILE"
  if command -v pg_dump >/dev/null 2>&1; then
    if PGPASSWORD="$DB_PASSWORD" pg_dump -h localhost -p 5433 -U "$DB_USER" -d "$DB_NAME" --no-owner --no-acl | gzip > "$OUT_FILE" 2>>"$LOG_FILE"; then
      echo "[$(date +'%F %T')] Host pg_dump backup succeeded: $OUT_FILE" | tee -a "$LOG_FILE"
    else
      echo "[$(date +'%F %T')] ERROR: host pg_dump failed." | tee -a "$LOG_FILE"
      if [ "$FORCE" = "true" ]; then
        echo "[$(date +'%F %T')] FORCE=true: continuing despite backup failure." | tee -a "$LOG_FILE"
      else
        echo "[$(date +'%F %T')] Aborting." | tee -a "$LOG_FILE"
        exit 3
      fi
    fi
  else
    echo "[$(date +'%F %T')] pg_dump not found on host and container not running. Cannot perform backup." | tee -a "$LOG_FILE"
    if [ "$FORCE" = "true" ]; then
      echo "[$(date +'%F %T')] FORCE=true: continuing despite no backup." | tee -a "$LOG_FILE"
    else
      exit 4
    fi
  fi
fi

echo "[$(date +'%F %T')] Running docker-compose down (safe) using $COMPOSE_FILE" | tee -a "$LOG_FILE"
# Do not remove volumes by default. If you want to remove volumes, set REMOVE_VOLUMES=true
if [ "${REMOVE_VOLUMES:-false}" = "true" ]; then
  docker-compose -f "$COMPOSE_FILE" down --volumes >>"$LOG_FILE" 2>&1
else
  docker-compose -f "$COMPOSE_FILE" down >>"$LOG_FILE" 2>&1
fi

echo "[$(date +'%F %T')] docker-compose down completed" | tee -a "$LOG_FILE"
echo "Log saved to $LOG_FILE"
echo "Backup path: $OUT_FILE"
exit 0

