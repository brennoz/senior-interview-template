# senior-interview-template

## Run

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test -Dtest="**/*Test,**/*IT" -DfailIfNoTests=false
```

## API — `http://localhost:8080/api/work-items`

All requests need `X-Forwarded-For`. IPs from CN, ES, US or cloud ISPs (Amazon, Google, Microsoft) → 403.

```bash
# Create
curl -s -X POST http://localhost:8080/api/work-items \
  -H "Content-Type: application/json" -H "X-Forwarded-For: 9.10.11.12" \
  -d '{"title":"Fix bug","priority":"HIGH"}' | jq

# List
curl -s "http://localhost:8080/api/work-items?page=0&size=5" -H "X-Forwarded-For: 9.10.11.12" | jq

# Get
curl -s http://localhost:8080/api/work-items/1 -H "X-Forwarded-For: 9.10.11.12" | jq

# Update status (OPEN → IN_PROGRESS → DONE → terminal)
curl -s -X PATCH http://localhost:8080/api/work-items/1/status \
  -H "Content-Type: application/json" -H "X-Forwarded-For: 9.10.11.12" \
  -d '{"status":"IN_PROGRESS"}' | jq

# Blocked country → 403  (CN: 1.0.1.1 · ES: 212.166.0.1 · US: 8.8.8.8)
curl -s http://localhost:8080/api/work-items -H "X-Forwarded-For: 1.0.1.1" | jq

# Blocked ISP → 403  (AWS: 52.0.0.1 · GCP: 34.0.0.1 · Azure: 20.0.0.1)
curl -s http://localhost:8080/api/work-items -H "X-Forwarded-For: 52.0.0.1" | jq
```

## H2 Console

`http://localhost:8080/h2-console` · JDBC: `jdbc:h2:mem:elemental-int` · User: `ec`

