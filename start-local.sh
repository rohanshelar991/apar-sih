#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="backend/.env.local"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "Create $ENV_FILE from backend/.env.local.example and set a local-only password and JWT secret." >&2
  exit 1
fi

# This command is intentionally pinned to loopback PostgreSQL and the dev profile.
# It never reads backend/.env, which is reserved for explicitly configured hosted deployments.
set -a
. "$ENV_FILE"
set +a
if [[ "${DB_URL:-}" != "jdbc:postgresql://localhost:5432/${POSTGRES_DB:-}" ]]; then
  echo "Refusing to start local mode: DB_URL must exactly match the local Compose database." >&2
  exit 1
fi
for variable in POSTGRES_DB POSTGRES_USER POSTGRES_PASSWORD DB_USERNAME DB_PASSWORD FLYWAY_USER FLYWAY_PASSWORD JWT_SECRET; do
  if [[ -z "${!variable:-}" || "${!variable}" == *replace-with* ]]; then
    echo "Set a real local value for $variable in backend/.env.local." >&2
    exit 1
  fi
done
if ! printf '%s' "$JWT_SECRET" | openssl base64 -d -A 2>/dev/null | wc -c | awk '$1 >= 48 { ok = 1 } END { exit !ok }'; then
  echo "JWT_SECRET must decode to at least 48 random bytes (generate one with: openssl rand -base64 48)." >&2
  exit 1
fi
if [[ "$DB_USERNAME" != "$POSTGRES_USER" || "$FLYWAY_USER" != "$POSTGRES_USER" || "$DB_PASSWORD" != "$POSTGRES_PASSWORD" || "$FLYWAY_PASSWORD" != "$POSTGRES_PASSWORD" ]]; then
  echo "Local Compose uses one owner role: DB and Flyway credentials must match POSTGRES_USER/PASSWORD." >&2
  exit 1
fi

docker compose --env-file "$ENV_FILE" up -d --wait postgres
SPRING_PROFILES_ACTIVE=postgres,dev SEED_DEMO_DATA=true mvn -f backend/pom.xml spring-boot:run &
BACKEND_PID=$!
npm run dev --prefix frontend &
FRONTEND_PID=$!
echo "Local app: http://localhost:5173 | API: http://localhost:8080"
trap 'kill "$BACKEND_PID" "$FRONTEND_PID" 2>/dev/null || true' EXIT INT TERM
wait
