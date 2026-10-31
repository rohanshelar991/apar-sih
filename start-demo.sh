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

echo "Starting Backend on Port 8080..."
SPRING_PROFILES_ACTIVE=h2 "$MVN_CMD" -f backend/pom.xml spring-boot:run &
BACKEND_PID=$!

echo "Starting Frontend on Port 5173..."
npm run dev --prefix frontend &
FRONTEND_PID=$!

echo "Backend PID: $BACKEND_PID | Frontend PID: $FRONTEND_PID"
echo "Swagger Docs: http://localhost:8080/swagger-ui/index.html"
echo "Frontend UI:  http://localhost:5173"

trap "kill $BACKEND_PID $FRONTEND_PID" EXIT
wait
