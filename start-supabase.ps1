# Loads the ignored backend/.env without echoing secrets or evaluating its contents.
# Runs PostgreSQL only. A connection/startup error exits; H2 is never a fallback.
$ErrorActionPreference = 'Stop'
$repoRoot = $PSScriptRoot
$localEnvPath = Join-Path $repoRoot 'backend/.env'
if (-not (Test-Path -LiteralPath $localEnvPath)) { throw 'Missing ignored backend/.env. Copy backend/.env.example and configure Supabase locally.' }
$allowedKeys = @('DB_URL','DB_USERNAME','DB_PASSWORD','FLYWAY_USER','FLYWAY_PASSWORD','DB_POOL_MAX','DB_POOL_MIN_IDLE','DB_CONNECTION_TIMEOUT_MS','JWT_SECRET','JWT_EXPIRY_MINUTES','VERIFY_TOKEN_SECRET','VERIFY_TOKEN_TTL_MINUTES','CORS_ALLOWED_ORIGINS')
$previousEnv = @{}
$loadedEnv = @{}
foreach ($line in [System.IO.File]::ReadAllLines($localEnvPath)) {
    if ($line -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=(.*)$' -and $allowedKeys -contains $Matches[1]) {
        $key = $Matches[1]
        $value = $Matches[2].Trim()
        if ($value.Length -ge 2 -and (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'")))) { $value = $value.Substring(1, $value.Length - 2) }
        $loadedEnv[$key] = $value
    }
}
foreach ($key in @('DB_URL','DB_USERNAME','DB_PASSWORD','FLYWAY_USER','FLYWAY_PASSWORD','JWT_SECRET')) {
    if (-not $loadedEnv[$key] -or $loadedEnv[$key] -match '^<.*>$') { throw "Missing required local setting: $key. Values are not logged." }
}
if ($loadedEnv['DB_URL'] -notmatch '^jdbc:postgresql://[^/]+:5432/' -or $loadedEnv['DB_URL'] -notmatch '[?&]sslmode=require(&|$)') { throw 'Supabase requires the Session Pooler JDBC URL on port 5432 with sslmode=require.' }
$javaPath = $null
if ($env:JAVA_HOME) { $candidate = Join-Path $env:JAVA_HOME 'bin/java.exe'; if (Test-Path -LiteralPath $candidate) { $javaPath = $candidate } }
if (-not $javaPath) { $portableJava = Get-ChildItem -LiteralPath (Join-Path $repoRoot 'tools/java') -Directory -ErrorAction SilentlyContinue | Select-Object -First 1; if ($portableJava) { $javaPath = Join-Path $portableJava.FullName 'bin/java.exe' } }
if (-not $javaPath) { $javaPath = (Get-Command java -ErrorAction Stop).Source }
$jarPath = Join-Path $repoRoot 'backend/target/apaar-connect-1.0.0-SNAPSHOT.jar'
if (-not (Test-Path -LiteralPath $jarPath)) { throw 'Build the backend first: mvn -f "backend/pom.xml" package' }
$loadedEnv['SPRING_PROFILES_ACTIVE'] = 'postgres'
$loadedEnv['SEED_DEMO_DATA'] = 'false'
try {
    foreach ($key in $loadedEnv.Keys) {
        $previousEnv[$key] = [Environment]::GetEnvironmentVariable($key, 'Process')
        [Environment]::SetEnvironmentVariable($key, $loadedEnv[$key], 'Process')
    }
    $previousEnv['TEMP'] = $env:TEMP; $previousEnv['TMP'] = $env:TMP
    $runtimeTemp = Join-Path $repoRoot 'tools/tmp'
    New-Item -ItemType Directory -Force -Path $runtimeTemp | Out-Null
    $env:TEMP = $runtimeTemp; $env:TMP = $runtimeTemp
    Write-Host 'Starting APAAR GARUDA NETRA: postgres profile, SSL required, Flyway enabled, Hibernate validate, seeding disabled, port 8080.'
    & $javaPath '-Duser.timezone=UTC' -jar $jarPath --spring.profiles.active=postgres --server.port=8080 --server.address=127.0.0.1 --spring.jpa.hibernate.ddl-auto=validate --spring.flyway.enabled=true --apaar.seed-demo-data=false
    if ($LASTEXITCODE -ne 0) { throw 'Supabase PostgreSQL startup failed. Check database reachability, credentials and schema validation. No H2 fallback was started.' }
} finally {
    foreach ($key in $previousEnv.Keys) { [Environment]::SetEnvironmentVariable($key, $previousEnv[$key], 'Process') }
}
