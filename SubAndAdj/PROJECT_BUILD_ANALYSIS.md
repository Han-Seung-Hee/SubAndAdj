# 현재 SubAndAdj 프로젝트 분석 리포트

## 📊 프로젝트 구조 분석

### 1. 디렉토리 구조

```
SubAndAdj (Root)
├── build.gradle ..................... 루트 빌드 설정 파일
├── settings.gradle .................. 모듈 정의 파일
├── gradle.properties ................. Gradle 설정
├── gradle/
│   └── wrapper/ ..................... Gradle 래퍼 (자동 설치)
├── saas-api/ ........................ 웹 애플리케이션 모듈
│   ├── build.gradle
│   ├── src/main/java/hsh/saas/saasapi/
│   │   └── SaasApiApplication.java
│   └── src/test/java/
├── saas-core/ ....................... 공유 라이브러리 모듈
│   ├── build.gradle
│   ├── src/main/java/hsh/saas/saascore/
│   └── src/test/java/
└── src/ ............................ 루트 프로젝트 소스 (선택사항)
    ├── main/java/
    └── test/java/
```

### 2. 프로젝트 역할 정의

| 영역 | 모듈 | 목적 | 특징 |
|------|------|------|------|
| **라이브러리** | `saas-core` | 공용 기능 제공 | jar 생성, bootJar 비활성화 |
| **애플리케이션** | `saas-api` | 최종 실행 가능 앱 | bootJar 생성, jar 비활성화 |

---

## 🔍 빌드 파일 상세 분석

### settings.gradle 분석

```groovy
rootProject.name = 'SubAndAdj'  // ✅ 루트 프로젝트 이름 지정
include 'saas-api'               // ✅ saas-api 모듈 포함
include 'saas-core'              // ✅ saas-core 모듈 포함
```

**역할:**
- Gradle이 인식해야 할 모든 서브프로젝트 정의
- 빌드 순서는 정의하지 않음 (의존성에 따라 결정됨)
- 모듈을 추가할 때마다 include 지시어 추가 필요

**주의사항:**
- 모든 모듈이 include되어야 Gradle이 인식
- 모듈명은 디렉토리명과 정확히 일치해야 함

---

### 루트 build.gradle 분석

```groovy
# ================== 1️⃣ 플러그인 정의 ==================
plugins {
    id 'org.springframework.boot' version '3.4.1' apply false
    # ✅ Spring Boot 플러그인
    # ✅ apply false: 버전만 정의, 서브프로젝트에서 선택적 적용
    # 🎯 역할: 서브프로젝트의 Spring Boot 버전 관리
    
    id 'io.spring.dependency-management' version '1.1.7' apply false
    # ✅ Spring 의존성 관리 플러그인
    # ✅ apply false: BOM(Bill of Materials)으로만 사용
    # 🎯 역할: Spring 라이브러리 버전 자동 관리
    
    id 'idea'
    # ✅ IntelliJ IDEA 지원
    # 🎯 역할: IDE 프로젝트 설정 생성
}

# ================== 2️⃣ IDE 설정 ==================
idea {
    module {
        downloadJavadoc = false   # ❌ Javadoc 다운로드 비활성화 (시간 절약)
        downloadSources = true    # ✅ 소스 코드 다운로드 활성화 (디버깅 용이)
    }
}

# ================== 3️⃣ 모든 프로젝트에 적용되는 설정 ==================
allprojects {
    group = 'hsh.saas'                    # ✅ 프로젝트 그룹 (groupId)
    version = '0.0.1-SNAPSHOT'            # ✅ 프로젝트 버전 (SNAPSHOT = 개발용)
    
    repositories {
        mavenCentral()                    # ✅ Maven Central Repository 사용
        # 추가 저장소 예시:
        # maven { url 'https://repo.example.com/maven' }
        # jcenter()
    }
    
    apply plugin: 'idea'                  # ✅ 모든 프로젝트에 idea 플러그인 적용
}

# ================== 4️⃣ 모든 서브프로젝트에만 적용되는 설정 ==================
subprojects {
    # --- 플러그인 적용 ---
    apply plugin: 'java'                  # ✅ Java 컴파일/테스트/패킹
    apply plugin: 'java-library'          # ✅ java 플러그인 + api 설정 지원
    # 💡 순서 중요: 일반적으로 java → java-library → spring.boot 순서
    
    apply plugin: 'org.springframework.boot'
    # ✅ Spring Boot 기능 활성화
    # 🎯 역할: 자동 설정, 내장 Tomcat, bootJar 생성 등
    
    apply plugin: 'io.spring.dependency-management'
    # ✅ Spring 라이브러리 버전 자동 해결
    # 🎯 역할: spring-boot-dependencies BOM 자동 적용

    # --- Java 버전 설정 ---
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
            # ✅ Java 17 사용
            # 🎯 역할: 각 모듈이 Java 17로 컴파일
            # 💡 toolchain: 시스템에 Java 17이 없으면 자동 다운로드
        }
    }

    # --- 공통 의존성 ---
    dependencies {
        # Lombok (어노테이션 기반 보일러플레이트 감소)
        compileOnly 'org.projectlombok:lombok'
        # ✅ compileOnly: 컴파일 시에만 필요, 런타임에는 불필요
        
        annotationProcessor 'org.projectlombok:lombok'
        # ✅ annotationProcessor: Lombok이 생성한 코드 처리
        
        # 테스트 의존성
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        # ✅ testImplementation: 테스트 시에만 포함
        
        testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
        # ✅ testRuntimeOnly: 테스트 런타임에만 필요
    }

    # --- 테스트 설정 ---
    tasks.named('test') {
        useJUnitPlatform()
        # ✅ JUnit 5 플랫폼 사용
        # 🎯 역할: Gradle이 JUnit 5 테스트를 인식하고 실행
    }
}
```

**설정의 논리적 흐름:**

1. **플러그인 정의** (plugins)
   - 어떤 기능을 사용할 것인가?
   - apply false: 버전만 정의, 실제 적용은 각 프로젝트에서 결정

2. **전체 프로젝트 설정** (allprojects)
   - 루트 + 모든 서브프로젝트에 적용
   - 그룹, 버전, 저장소 등 공통 설정

3. **서브프로젝트 설정** (subprojects)
   - 루트 제외, 모든 서브프로젝트만 적용
   - Java 컴파일, 의존성, 테스트 등 개발 환경 설정

---

### saas-core/build.gradle 분석

```groovy
dependencies {
    # ===== API 의존성 (다른 모듈에 노출됨) =====
    api 'org.springframework.boot:spring-boot-starter-data-jpa'
    # ✅ api(): java-library 플러그인에서만 지원
    # 🎯 역할: 이 라이브러리를 사용하는 모듈도 자동으로 JPA 사용 가능
    # 💡 saas-api가 saas-core를 의존하면, 
    #    saas-api는 자동으로 JPA를 사용할 수 있음
    # 📝 언제 사용?
    #    - public 클래스가 JPA Entity를 반환
    #    - 다른 모듈이 JPA를 사용해야 함

    # ===== 구현 의존성 (내부용, 노출 안됨) =====
    implementation 'org.postgresql:postgresql'
    # ✅ implementation: 이 모듈 내부에서만 사용
    # 🎯 역할: saas-api는 PostgreSQL을 직접 사용할 수 없음
    # 📝 언제 사용?
    #    - 데이터베이스 드라이버 (구현 세부사항)
    #    - 내부 유틸리티 라이브러리
    
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    # 💡 Redis도 구현 의존성인 이유:
    #    saas-core가 Redis를 internal detail로 사용
    #    saas-api는 saas-core의 public API만 알면 됨
}

# ===== JAR 설정 =====
bootJar { enabled = false }
# ✅ bootJar 비활성화
# 🎯 이유: saas-core는 라이브러리, 독립적으로 실행 불가능
# 📝 생성되지 않음: saas-core-0.0.1-SNAPSHOT.jar (실행 불가)

jar { enabled = true }
# ✅ 일반 jar 활성화
# 🎯 생성됨: saas-core-0.0.1-SNAPSHOT-plain.jar (라이브러리로 사용)
# 📝 다른 모듈이 의존할 수 있는 라이브러리 JAR
```

**의존성 선택 기준:**

| 상황 | 설정 | 이유 |
|------|------|------|
| 다른 모듈의 public API에서 사용되는 클래스 | `api` | 다른 모듈도 해당 라이브러리 필요 |
| 이 모듈의 private 구현 세부사항 | `implementation` | 다른 모듈은 알 필요 없음 |
| 데이터베이스 드라이버 | `implementation` | 구현 세부사항 |
| 웹 프레임워크 (내부 사용) | `implementation` | API 모듈에서 다시 정의 |
| 공통 유틸리티 라이브러리 | `api` | 모든 모듈이 사용 가능 |

---

### saas-api/build.gradle 분석

```groovy
dependencies {
    # ===== 모듈 의존성 =====
    implementation project(':saas-core')
    # ✅ 동일 프로젝트의 다른 모듈에 의존
    # 🎯 역할: saas-core의 모든 public API 사용 가능
    # 📝 project(':saas-core')
    #    - ':' prefix: 루트에서의 경로
    #    - saas-core의 모든 api 의존성 자동 포함

    # ===== 외부 의존성 =====
    implementation 'org.springframework.boot:spring-boot-starter-web'
    # ✅ REST API 기능 (Spring MVC, Tomcat 포함)
}

# ===== JAR 설정 =====
bootJar { enabled = true }
# ✅ bootJar 활성화
# 🎯 생성됨: saas-api-0.0.1-SNAPSHOT.jar (실행 가능)
# 📝 java -jar saas-api-0.0.1-SNAPSHOT.jar로 실행 가능

jar { enabled = false }
# ✅ 일반 jar 비활성화
# 🎯 생성 안됨: saas-api-0.0.1-SNAPSHOT-plain.jar 불필요
# 📝 실행 가능한 jar만 필요하므로
```

---

## 🎯 의존성 전파 흐름 (매우 중요!)

### api vs implementation의 차이

```
예시: saas-api ─depends on─> saas-core

saas-core/build.gradle:
┌──────────────────────────────────────┐
│ api 'org.springframework.boot:       │
│     spring-boot-starter-data-jpa'   │
│                                      │
│ implementation 'org.postgresql:      │
│               postgresql'            │
└──────────────────────────────────────┘
         │                    │
         └────┬───────────────┘
              │
              ▼ 
      saas-api가 보는 것:
      ┌──────────────────────────────────┐
      │ ✅ JPA (api로 전파)               │
      │ ❌ PostgreSQL (구현 세부사항)    │
      │ ✅ project(':saas-core')의        │
      │    모든 소스 코드 직접 접근      │
      └──────────────────────────────────┘
```

### 구체적인 예시

```java
// saas-core의 공개 인터페이스
public class UserEntity { // JPA Entity
    @Id
    private Long id;
}

public class UserRepository 
    extends JpaRepository<UserEntity, Long> {
    // JPA 사용
}
```

```java
// saas-api에서 saas-core 사용
@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository; // ✅ 사용 가능
    
    @GetMapping("/users")
    public List<UserEntity> getUsers() {
        return userRepository.findAll();
        // ✅ JPA 사용 가능 (api로 전파되었음)
    }
    
    // ❌ PostgreSQL 직접 사용 불가
    // Connection conn = DriverManager.getConnection(...);
}
```

---

## 📈 빌드 결과물 분석

### 빌드 후 생성되는 파일

```
saas-core/build/libs/
├── saas-core-0.0.1-SNAPSHOT.jar (일반 라이브러리 JAR)
│   ├── 내용: 컴파일된 클래스 + resources
│   ├── 사용처: 다른 모듈이 의존성으로 사용
│   └── 실행 불가능

saas-api/build/libs/
├── saas-api-0.0.1-SNAPSHOT.jar (실행 가능한 Spring Boot JAR)
│   ├── 내용: 컴파일된 클래스 + 모든 의존성 + Spring Boot loader
│   ├── 사용처: 프로덕션 배포
│   └── 실행: java -jar saas-api-0.0.1-SNAPSHOT.jar
│
└── saas-api-0.0.1-SNAPSHOT-plain.jar (X) 생성 안됨
```

### 의존성 계층

```
saas-api
  ├─ project(':saas-core')
  │   ├─ api: spring-boot-starter-data-jpa
  │   │   ├─ org.springframework.boot:spring-boot-starter-orm:3.4.1
  │   │   ├─ org.hibernate.orm:hibernate-core:...
  │   │   └─ ...
  │   ├─ implementation: postgresql (다른 모듈에 미전파)
  │   └─ ...
  │
  └─ implementation: spring-boot-starter-web
      ├─ org.springframework:spring-webmvc:...
      ├─ org.springframework.boot:spring-boot-starter-tomcat:...
      └─ ...
```

---

## 🔧 문제 해결 가이드

### 발생했던 에러 분석

#### 에러 1: Could not find method api()
```
Could not find method api() for arguments [org.springframework.boot:...]
on object of type org.gradle.api.internal.artifacts.dsl.dependencies...
```

**원인:**
```groovy
// ❌ java-library 플러그인 없음
subprojects {
    apply plugin: 'java'  // ← java 플러그인만 적용
    // api() 설정 사용 불가능!
}
```

**해결:**
```groovy
// ✅ java-library 플러그인 추가
subprojects {
    apply plugin: 'java'
    apply plugin: 'java-library'  // ← 이 줄 추가
}
```

#### 에러 2: Could not locate module
```
Could not locate module for project ':saas-core'
```

**원인:**
```groovy
// ❌ settings.gradle에 모듈이 정의되지 않음
// include 'saas-core'  ← 이 줄이 없음
```

**해결:**
```groovy
// ✅ settings.gradle에 모듈 추가
include 'saas-core'
```

---

## ✅ 현재 설정의 강점

1. **✅ java-library 플러그인 적용**
   - api/implementation 구분 가능
   - 올바른 의존성 관리

2. **✅ 명확한 모듈 역할 정의**
   - saas-core: 라이브러리 (bootJar disabled)
   - saas-api: 애플리케이션 (bootJar enabled)

3. **✅ Spring Boot 버전 통일**
   - apply false로 버전 관리
   - 모든 서브프로젝트가 동일 버전 사용

4. **✅ 공통 의존성 관리**
   - Lombok 중앙 관리
   - 테스트 설정 통일

---

## 🚀 향후 개선 제안

### 1. gradle.properties 도입

**현재:**
```groovy
plugins {
    id 'org.springframework.boot' version '3.4.1' apply false
}
```

**개선:**
```groovy
// gradle.properties
springBootVersion=3.4.1
postgresqlVersion=42.7.0

// build.gradle
plugins {
    id 'org.springframework.boot' version "${springBootVersion}" apply false
}
```

**장점:** 버전 한 곳에서 관리

### 2. 조건부 설정

```groovy
subprojects {
    // 라이브러리 모듈 구분
    def isLibrary = ['saas-core'].contains(project.name)
    
    if (isLibrary) {
        bootJar { enabled = false }
        jar { enabled = true }
    } else {
        bootJar { enabled = true }
        jar { enabled = false }
    }
}
```

### 3. 더 많은 모듈 추가 시

```groovy
// settings.gradle에 추가
include 'saas-common'      // 공통 라이브러리
include 'saas-service'     // 비즈니스 로직
include 'saas-web'         // REST API
```

### 4. 성능 최적화

```groovy
// gradle.properties에 추가
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.workers.max=8
```

---

## 📚 현재 설정 요약

| 항목 | 현재 설정 | 평가 |
|------|---------|------|
| 플러그인 구조 | java, java-library, spring-boot | ✅ 올바름 |
| 의존성 관리 | api/implementation 구분 | ✅ 올바름 |
| JAR 생성 | bootJar/jar 조건부 | ✅ 올바름 |
| 모듈 정의 | settings.gradle 명확 | ✅ 올바름 |
| 버전 관리 | 하드코딩 | ⚠️ gradle.properties 권장 |
| 문서화 | 주석 부족 | ⚠️ 개선 필요 |

---

## 참고: 다른 멀티모듈 패턴

### 단순 구조 (현재)
```
Root
├── core
└── api
```
→ 2개 모듈, 1:1 의존성

### 계층형 구조
```
Root
├── common
├── core
├── api
└── web-service
```
→ 명확한 계층, 순차적 의존성

### 마이크로서비스
```
Root
├── common
├── user-service
├── order-service
└── api-gateway
```
→ 독립적 서비스, 선택적 의존성

현재 프로젝트는 "단순 구조"로, 향후 "계층형" 또는 "마이크로서비스" 패턴으로 확장 가능합니다.

