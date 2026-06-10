# Infrastructure Setup

## Services

The local Agent stack expects these services:

- MySQL 8.4: business relational data.
- Redis 7.2: auth token/session/hot memory infrastructure.
- MinIO: object storage and presigned upload URLs.
- Chroma 1.5.10.dev103: vector collections with tenant/database isolation and the `/api/v2` API.

Start local infrastructure:

```powershell
docker compose -f docker-compose.infra.yml up -d
```

Copy env template:

```powershell
Copy-Item .env.example .env
```

Key defaults:

```properties
HOPE_DB_URL=jdbc:mysql://localhost:3307/hope_sparks?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
HOPE_DB_USERNAME=hope
HOPE_DB_PASSWORD=hope123456
HOPE_REDIS_HOST=localhost
HOPE_REDIS_PORT=6380
HOPE_REDIS_PASSWORD=
CHROMA_ENABLED=true
CHROMA_API_VERSION=v2
CHROMA_BASE_URL=http://localhost:8000
MINIO_ENABLED=true
MINIO_ENDPOINT=http://localhost:9000
MINIO_PUBLIC_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=hope-sparks
KB_PENDING_BATCH_SIZE=10
KB_CHUNK_MAX_CHARACTERS=1200
KB_CHUNK_OVERLAP_CHARACTERS=180
KB_CHUNK_SEMANTIC_THRESHOLD=0.82
KB_CHUNK_ENABLE_SEMANTIC=true
KB_OCR_ENABLED=false
KB_OCR_COMMAND=tesseract
KB_OCR_LANGUAGE=chi_sim+eng
```

Run backend:

```powershell
$env:JAVA_HOME='C:\Users\26465\.jdks\ms-21.0.8'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn -q -pl arch-boot -am -DskipTests "-Dmaven.compiler.fork=true" spring-boot:run
```

Check health:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

## Current Adapter Status

- MySQL: Spring Boot datasource is configured. The compose MySQL maps to host port `3307` to avoid conflicts with an existing local MySQL on `3306`.
- Redis: Spring Data Redis is configured. The compose Redis maps to host port `6380` to avoid conflicts with an existing local Redis on `6379`.
- MinIO: real `MinioFileStorageService` creates bucket and presigned PUT URLs.
- Chroma: local compose now runs `chromadb/chroma:1.5.10.dev103` and should be checked through `http://localhost:8000/api/v2/heartbeat`.
- KB ingest: `txt/pdf/docx/pptx/url/video-url/image` route parsing is wired in code; OCR depends on a locally available `tesseract` executable when `KB_OCR_ENABLED=true`.
