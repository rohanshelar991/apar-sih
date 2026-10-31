@echo off
setlocal
set "SPRING_PROFILES_ACTIVE=h2"
echo ==============================================================================
echo        AICTE APAAR GARUDA NETRA: Student Data Management Platform
echo              Smart India Hackathon 2026 - Problem Statement 26263
echo                         Developed by Team INFOBRO
echo ==============================================================================
echo.
echo Starting Backend (Spring Boot 3 + In-Memory H2 in PostgreSQL Mode)...
start "APAAR GARUDA NETRA Backend (Port 8080)" cmd /k "tools\maven\bin\mvn.cmd -f backend\pom.xml spring-boot:run"

echo Starting Frontend (React 18 + Vite on Port 5173)...
start "APAAR GARUDA NETRA Frontend (Port 5173)" cmd /k "npm run dev --prefix frontend"

echo.
echo ==============================================================================
echo Application is initializing!
echo Backend Swagger API Docs: http://localhost:8080/swagger-ui/index.html
echo Frontend Application UI:  http://localhost:5173
echo.
echo Demo Persona Credentials:
echo   1. Institution Admin:   admin@bmsce.ac.in      / Admin@123
echo   2. AICTE Regulator:     regulator@aicte-india.org / Regulator@123
echo   3. Public Verifier:     verifier@tcs.com       / Verifier@123
echo ==============================================================================
