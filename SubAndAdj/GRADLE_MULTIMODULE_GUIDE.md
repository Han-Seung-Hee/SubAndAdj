# Gradle 멀티모듈 프로젝트 설정 가이드

이 문서는 Spring Boot 3.x + Gradle을 사용하는 멀티모듈 프로젝트의 올바른 설정 방법을 설명합니다.

## 📋 목차
1. [프로젝트 구조](#프로젝트-구조)
2. [에러 원인 분석](#에러-원인-분석)
3. [설정 방법](#설정-방법)
4. [모범 사례](#모범-사례)
5. [일반적인 문제 해결](#일반적인-문제-해결)

---

## 프로젝트 구조

### 현재 프로젝트 구조

```
SubAndAdj (루트 프로젝트)
├── build.gradle (루트 빌드 설정)
├── settings.gradle (모듈 정의)
├── saas-api/ (실행 가능한 애플리케이션 모듈)
│   ├── build.gradle
│   └── src/
├── saas-core/ (라이브러리 모듈)
│   ├── build.gradle
│   └── src/
└── src/ (루트 프로젝트 소스 코드)
```

### 모듈의 역할

| 모듈 | 용도 | 특징 |
|------|------|------|
| **saas-core** | 공유 라이브러리 | 다른 모듈이 의존하는 공통 코드 포함, bootJar 비활성화 |
| **saas-api** | 실행 가능한 애플리케이션 | 최종 실행 가능한 JAR 생성, saas-core 의존 |

---

## 에러 원인 분석

### ❌ 발생한 에러

```
Could not find method api() for arguments [org.springframework.boot:spring-boot-starter-data-jpa] 
on object of type org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler.
```

### 🔍 원인

1. **`api()` 설정을 사용했는데 `java-library` 플러그인이 없음**
   - `api()` 설정은 `java-library` 플러그인에서만 제공됨
   - 일반 `java` 플러그인에서는 지원하지 않음

2. **설정 키워드의 차이**
   - `implementation`: 이 프로젝트 내부에서만 사용되는 의존성
   - `api` (java-library): 이 라이브러리를 의존하는 다른 프로젝트에 자동으로 노출됨

### 📊 설정 비교

| 설정 키워드 | java 플러그인 | java-library 플러그인 | 설명 |
|-----------|--------------|-------------------|------|
| `implementation` | ✅ | ✅ | 내부 구현만 제공 |
| `api` | ❌ | ✅ | 공개 인터페이스 제공 |
| `testImplementation` | ✅ | ✅ | 테스트 전용 |
| `runtimeOnly` | ✅ | ✅ | 런타임에만 필요 |

---

## 설정 방법

### 1️⃣ 루트 build.gradle 설정

```groovy
plugins {
    id 'org.springframework.boot' version '3.4.1' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
    id 'idea'
}

idea {
    module {
        downloadJavadoc = false
        downloadSources = true
    }
}

allprojects {
    group = 'hsh.saas'
    version = '0.0.1-SNAPSHOT'

    repositories {
        mavenCentral()
    }

    apply plugin: 'idea'
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'java-library'  // ✅ 모든 서브프로젝트에 적용
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    dependencies {
        compileOnly 'org.projectlombok:lombok'
        annotationProcessor 'org.projectlombok:lombok'
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    }

    tasks.named('test') {
        useJUnitPlatform()
    }
}
```

**주요 포인트:**
- ✅ `apply plugin: 'java-library'` 적용
- ✅ Spring Boot 플러그인은 `apply false` 사용 (버전만 정의)
- ✅ dependency-management는 `apply false` 사용

### 2️⃣ settings.gradle 설정

```groovy
rootProject.name = 'SubAndAdj'
include 'saas-api'
include 'saas-core'
```

**주요 포인트:**
- ✅ 모든 서브프로젝트를 `include` 지시어로 명시
- ✅ 루트 프로젝트 이름 설정
- ❌ 주석이나 복잡한 로직 피하기

### 3️⃣ 라이브러리 모듈 build.gradle (saas-core)

```groovy
dependencies {
    // API 의존성: 이 라이브러리를 사용하는 다른 모듈에 자동 노출
    api 'org.springframework.boot:spring-boot-starter-data-jpa'
    
    // 구현 의존성: 이 모듈 내부에서만 사용, 다른 모듈에 노출 안됨
    implementation 'org.postgresql:postgresql'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
}

// core는 라이브러리이므로 실행 파일(bootJar)을 만들지 않습니다.
bootJar { enabled = false }
jar { enabled = true }
```

**설정 규칙:**
- 다른 모듈이 필요로 하는 의존성: `api` 사용
- 내부 구현 전용: `implementation` 사용
- 공개 인터페이스(class, interface)를 정의하는 의존성은 `api`

### 4️⃣ 애플리케이션 모듈 build.gradle (saas-api)

```groovy
dependencies {
    // 다른 모듈 의존성: project() 사용
    implementation project(':saas-core')
    
    // 웹 애플리케이션 스타터
    implementation 'org.springframework.boot:spring-boot-starter-web'
}

// API는 실행 가능한 jar가 필요하므로 bootJar를 활성화합니다.
bootJar { enabled = true }
jar { enabled = false }
```

**설정 규칙:**
- 모듈 간 의존성: `project(':모듈명')` 형식
- 최종 실행 모듈: `bootJar enabled = true`
- 부분 실행 모듈: `bootJar enabled = false`

---

## 모범 사례

### ✅ DO (해야 할 것)

1. **라이브러리 모듈 구분**
   ```groovy
   // 라이브러리 모듈: core, common 등
   bootJar { enabled = false }
   jar { enabled = true }
   ```

2. **애플리케이션 모듈 구분**
   ```groovy
   // 애플리케이션 모듈: api, service 등
   bootJar { enabled = true }
   jar { enabled = false }
   ```

3. **의존성 노출 선택**
   ```groovy
   // 공개 API (다른 모듈이 사용할 클래스/인터페이스)
   api 'org.springframework.boot:spring-boot-starter-data-jpa'
   
   // 내부 구현 (이 모듈만 사용)
   implementation 'org.postgresql:postgresql'
   ```

4. **버전 일관성 유지**
   - 루트 gradle.properties에서 버전 정의
   ```groovy
   // gradle.properties
   springBootVersion = 3.4.1
   postgresqlVersion = 42.7.0
   ```

5. **명확한 구조**
   ```
   root/ (빌드 환경, 의존성 관리)
   ├── common-library/ (공유 라이브러리)
   ├── user-service/ (독립적인 애플리케이션)
   ├── order-service/ (독립적인 애플리케이션)
   └── api-gateway/ (외부 진입점)
   ```

### ❌ DON'T (피해야 할 것)

1. **모든 모듈을 bootJar로 설정**
   ```groovy
   // ❌ 나쁜 예: 라이브러리도 bootJar 활성화
   bootJar { enabled = true }
   jar { enabled = true }
   ```

2. **라이브러리에서만 api() 사용하고 다른 곳에서는 implementation 사용**
   ```groovy
   // ❌ 일관성 없음
   implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
   ```

3. **루트 프로젝트에 비즈니스 로직 포함**
   - 루트는 빌드 환경 설정만 담당

4. **모듈 간 순환 의존성**
   ```groovy
   // ❌ saas-api → saas-core → saas-api (순환!)
   // 가능하면 단방향 의존성 구조 유지
   ```

---

## 일반적인 문제 해결

### 문제 1: "Could not find method api()"

**증상:**
```
Could not find method api() for arguments [...]
```

**해결:**
```groovy
// 루트 build.gradle의 subprojects에 추가
apply plugin: 'java-library'  // ← 이 줄이 필수
```

### 문제 2: "Project not found"

**증상:**
```
Could not locate module for project ':unknown-module'
```

**해결:**
```groovy
// settings.gradle에서 모든 모듈을 include
include 'saas-api'      // ← 누락된 모듈 추가
include 'saas-core'
```

### 문제 3: 모듈이 IntelliJ에서 인식되지 않음

**증상:**
- 모듈 목록에 나타나지 않음
- 빨간 밑줄 표시

**해결:**
1. IntelliJ IDEA → 메뉴 → File → Invalidate Caches
2. Gradle 탭 → 새로고침 버튼 클릭
3. 터미널에서 실행:
   ```bash
   ./gradlew clean build
   ```

### 문제 4: 불필요한 bootJar 생성

**증상:**
```
경로: build/libs/saas-core-0.0.1-SNAPSHOT.jar (bootJar와 plain jar 모두 생성)
```

**해결:**
```groovy
// 라이브러리 모듈에서 명확히 설정
bootJar { 
    enabled = false  // 부트 애플리케이션 JAR 생성 안함
}
jar { 
    enabled = true   // 일반 라이브러리 JAR만 생성
}
```

### 문제 5: "Task 'prepareKotlinBuildScriptModel' not found"

**증상:**
```
Task 'prepareKotlinBuildScriptModel' not found in project ':saas-api'
```

**원인:**
- IDE 캐시 문제 또는 부분적으로 초기화된 모듈

**해결:**
```bash
# 1. 캐시 삭제
./gradlew clean

# 2. IDE 캐시 삭제 (IntelliJ)
# File → Invalidate Caches → Invalidate and Restart

# 3. 다시 빌드
./gradlew build
```

---

## 체크리스트

프로젝트 설정 완료 확인:

- [ ] `settings.gradle`에 모든 모듈 include 됨
- [ ] 루트 `build.gradle`에 `java-library` 플러그인 적용
- [ ] 라이브러리 모듈: `bootJar { enabled = false }`, `jar { enabled = true }`
- [ ] 애플리케이션 모듈: `bootJar { enabled = true }`, `jar { enabled = false }`
- [ ] 라이브러리 모듈에서 `api()` 사용 가능
- [ ] 애플리케이션 모듈에서 `project(':모듈명')` 의존성 추가
- [ ] `./gradlew build` 성공

---

## 추가 리소스

### 공식 문서
- [Gradle Java Library Plugin](https://docs.gradle.org/current/userguide/java_library_plugin.html)
- [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/)
- [Gradle Dependency Management](https://docs.gradle.org/current/userguide/dependency_management.html)

### 자주 보는 설정 템플릿

**gradle.properties (선택사항)**
```properties
# JDK 버전
java.version=17

# Spring Boot 버전
springBootVersion=3.4.1
springCloudVersion=2024.0.0

# 데이터베이스
postgresqlVersion=42.7.0
```

**모듈별 권장 구조**
```
src/
├── main/
│   ├── java/
│   │   └── hsh/saas/
│   │       ├── subandadj/ (루트)
│   │       ├── saasapi/ (saas-api 모듈)
│   │       └── saascore/ (saas-core 모듈)
│   └── resources/
└── test/
    ├── java/
    └── resources/
```

---

## 최종 요약

멀티모듈 Gradle 프로젝트 설정의 핵심:

1. **루트 설정**: 공통 환경 설정 (플러그인, 의존성 관리)
2. **모듈 정의**: `settings.gradle`에서 명시
3. **플러그인 선택**: 라이브러리는 `java-library`, 애플리케이션은 `java`
4. **의존성 관리**: `api` vs `implementation` 구분
5. **JAR 생성**: 역할에 맞게 `bootJar`/`jar` 설정

이 가이드를 따르면 대부분의 멀티모듈 설정 문제를 해결할 수 있습니다.

