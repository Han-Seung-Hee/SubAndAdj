# SubAndAdj

SubAndAdj is a Gradle multi-module Spring Boot service.

## Overview

- `saas-api`: executable API module
- `saas-core`: shared core/domain module

## Stack

- Java 25
- Spring Boot 3.5.13
- PostgreSQL
- Redis
- Gradle (multi-module)

## Run Local

```bash
cd /Users/xorhd1222/Programming/Project/SubAndAdj/SubAndAdj
docker compose up -d postgres redis
./gradlew :saas-api:bootRun
```

## Local Ports

- App: `http://localhost:18080`
- PostgreSQL: `localhost:5433`
- Redis: `localhost:6380`

## Build

```bash
./gradlew clean build -x test
./gradlew test
```

