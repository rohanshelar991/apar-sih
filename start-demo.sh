#!/usr/bin/env bash
echo "=============================================================================="
echo "       AICTE APAAR GARUDA NETRA: Student Data Management Platform"
echo "             Smart India Hackathon 2026 - Problem Statement 26263"
echo "                        Developed by Team INFOBRO"
echo "=============================================================================="

# Check if embedded maven or system mvn exists
if [ -f "./tools/maven/bin/mvn" ]; then
    MVN_CMD="./tools/maven/bin/mvn"
else
    MVN_CMD="mvn"
fi

# The backend must use only the approved Supabase PostgreSQL project.
if [ ! -f "backend/.env" ]; then
    echo "backend/.env is required and must contain the rotated credentials for the approved Supabase project." >&2
    exit 1
fi
set -a
. "backend/.env"
set +a
if [ "${SUPABASE_PROJECT_REF:-}" != "hxmlezblzvdbacebsmcb" ]; then
    echo "Refusing to start: backend/.env is not configured for the approved Supabase project." >&2
    exit 1
fi
for variable in DB_URL DB_USERNAME DB_PASSWORD FLYWAY_USER FLYWAY_PASSWORD JWT_SECRET; do
    if [ -z "${!variable:-}" ]; then
        echo "Refusing to start: a required private backend setting is missing from backend/.env." >&2
        exit 1
    fi
done
echo "Starting Backend on Port 8080 with Supabase PostgreSQL..."
SPRING_PROFILES_ACTIVE=postgres "$MVN_CMD" -f backend/pom.xml spring-boot:run &
BACKEND_PID=$!

echo "Starting Frontend on Port 5173..."
npm run dev --prefix frontend &
FRONTEND_PID=$!

echo "Backend PID: $BACKEND_PID | Frontend PID: $FRONTEND_PID"
echo "Swagger Docs: http://localhost:8080/swagger-ui/index.html"
echo "Frontend UI:  http://localhost:5173"

trap "kill $BACKEND_PID $FRONTEND_PID" EXIT
wait
