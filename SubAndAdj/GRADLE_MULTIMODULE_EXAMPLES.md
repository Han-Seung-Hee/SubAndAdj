# Gradle 멀티모듈 설정 실습 예제

이 문서는 실제 적용 가능한 다양한 멀티모듈 구성 예제를 제공합니다.

## 📌 예제 1: 기본 2단계 구조 (현재 프로젝트)

### 구조
```
SubAndAdj/
├── saas-api (실행 가능한 애플리케이션)
└── saas-core (공유 라이브러리)
```

### 설정 파일

**settings.gradle**
```groovy
rootProject.name = 'SubAndAdj'
include 'saas-api'
include 'saas-core'
```

**build.gradle (루트)**
```groovy
plugins {
    id 'org.springframework.boot' version '3.4.1' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}

allprojects {
    group = 'hsh.saas'
    version = '0.0.1-SNAPSHOT'
    repositories { mavenCentral() }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'java-library'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    java {
        toolchain { languageVersion = JavaLanguageVersion.of(17) }
    }

    dependencies {
        compileOnly 'org.projectlombok:lombok'
        annotationProcessor 'org.projectlombok:lombok'
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
    }
}
```

**saas-core/build.gradle**
```groovy
dependencies {
    api 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.postgresql:postgresql'
}

bootJar { enabled = false }
jar { enabled = true }
```

**saas-api/build.gradle**
```groovy
dependencies {
    implementation project(':saas-core')
    implementation 'org.springframework.boot:spring-boot-starter-web'
}

bootJar { enabled = true }
jar { enabled = false }
```

---

## 📌 예제 2: 3단계 구조 (권장 아키텍처)

마이크로서비스 아키텍처에서 흔한 구조:

### 구조
```
MicroserviceApp/
├── common-library (공통 코드)
├── user-service (독립적 애플리케이션)
├── order-service (독립적 애플리케이션)
└── api-gateway (진입점)
```

### 설정 파일

**settings.gradle**
```groovy
rootProject.name = 'MicroserviceApp'
include 'common-library'
include 'user-service'
include 'order-service'
include 'api-gateway'
```

**build.gradle (루트)**
```groovy
plugins {
    id 'org.springframework.boot' version '3.4.1' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}

allprojects {
    group = 'com.microservice'
    version = '1.0.0'
    repositories { mavenCentral() }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'java-library'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    java {
        toolchain { languageVersion = JavaLanguageVersion.of(17) }
    }

    // 공통 의존성
    dependencies {
        // Lombok
        compileOnly 'org.projectlombok:lombok'
        annotationProcessor 'org.projectlombok:lombok'
        
        // 테스트
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        
        // 로깅
        implementation 'org.springframework.boot:spring-boot-starter-logging'
    }
}
```

**common-library/build.gradle** (다른 모든 모듈에 의존)
```groovy
dependencies {
    // 공통으로 사용할 의존성들
    api 'org.springframework.boot:spring-boot-starter-data-jpa'
    api 'org.springframework.boot:spring-boot-starter-data-redis'
    api 'com.fasterxml.jackson.core:jackson-databind'
    
    // 내부 구현 전용
    implementation 'org.postgresql:postgresql'
}

bootJar { enabled = false }
jar { enabled = true }
```

**user-service/build.gradle**
```groovy
dependencies {
    implementation project(':common-library')
    implementation 'org.springframework.boot:spring-boot-starter-web'
}

bootJar { enabled = true }
jar { enabled = false }
```

**order-service/build.gradle**
```groovy
dependencies {
    implementation project(':common-library')
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
}

bootJar { enabled = true }
jar { enabled = false }
```

**api-gateway/build.gradle**
```groovy
dependencies {
    implementation project(':common-library')
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
}

bootJar { enabled = true }
jar { enabled = false }
```

---

## 📌 예제 3: 공유 라이브러리 전문 구조

라이브러리를 여러 개 모듈로 나누는 경우:

### 구조
```
LibraryProject/
├── core-lib (핵심 공통)
├── database-lib (데이터베이스 관련)
├── api-lib (API 관련)
├── app-service (실제 애플리케이션)
└── cli-service (CLI 애플리케이션)
```

### 설정 파일

**settings.gradle**
```groovy
rootProject.name = 'LibraryProject'
include 'core-lib'
include 'database-lib'
include 'api-lib'
include 'app-service'
include 'cli-service'
```

**build.gradle (루트)**
```groovy
plugins {
    id 'org.springframework.boot' version '3.4.1' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
    id 'java'
    id 'java-library'
}

allprojects {
    group = 'com.library'
    version = '2.0.0'
    repositories { mavenCentral() }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'java-library'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    java {
        toolchain { languageVersion = JavaLanguageVersion.of(17) }
    }

    dependencies {
        compileOnly 'org.projectlombok:lombok'
        annotationProcessor 'org.projectlombok:lombok'
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
    }
}

// 라이브러리 모듈 설정
configure([project(':core-lib'), 
           project(':database-lib'), 
           project(':api-lib')]) {
    bootJar { enabled = false }
    jar { enabled = true }
}

// 애플리케이션 모듈 설정
configure([project(':app-service'), 
           project(':cli-service')]) {
    bootJar { enabled = true }
    jar { enabled = false }
}
```

**core-lib/build.gradle**
```groovy
dependencies {
    api 'com.google.guava:guava:33.0.0-jre'
    api 'com.fasterxml.jackson.core:jackson-databind'
    api 'org.slf4j:slf4j-api'
}
```

**database-lib/build.gradle**
```groovy
dependencies {
    api project(':core-lib')
    api 'org.springframework.boot:spring-boot-starter-data-jpa'
    api 'org.springframework.boot:spring-boot-starter-data-redis'
    
    implementation 'org.postgresql:postgresql'
    implementation 'org.flywaydb:flyway-core'
}
```

**api-lib/build.gradle**
```groovy
dependencies {
    api project(':core-lib')
    api 'org.springframework.boot:spring-boot-starter-web'
    
    implementation 'io.springfox:springfox-boot-starter:3.0.0'
}
```

**app-service/build.gradle**
```groovy
dependencies {
    implementation project(':core-lib')
    implementation project(':database-lib')
    implementation project(':api-lib')
}
```

**cli-service/build.gradle**
```groovy
dependencies {
    implementation project(':core-lib')
    implementation project(':database-lib')
}
```

---

## 📌 예제 4: gradle.properties 활용한 버전 관리

**gradle.properties**
```properties
# Java 버전
java.version=17

# Spring 버전
springBootVersion=3.4.1
springCloudVersion=2024.0.0
springDependencyManagementVersion=1.1.7

# 데이터베이스
postgresqlVersion=42.7.0
h2Version=2.3.0

# 라이브러리
lombokVersion=1.18.30
guavaVersion=33.0.0-jre
jacksonVersion=2.17.0

# 테스트
junitVersion=5.10.2
junitPlatformVersion=1.10.2
```

**build.gradle (루트) - gradle.properties 사용**
```groovy
plugins {
    id 'org.springframework.boot' version "${springBootVersion}" apply false
    id 'io.spring.dependency-management' version "${springDependencyManagementVersion}" apply false
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'java-library'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(java.version)
        }
    }

    dependencies {
        compileOnly "org.projectlombok:lombok:${lombokVersion}"
        annotationProcessor "org.projectlombok:lombok:${lombokVersion}"
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
    }
}
```

---

## 🔧 실전 팁

### 1. 의존성 버전 충돌 해결

```groovy
// build.gradle (서브프로젝트)
dependencies {
    implementation project(':common-library')
    
    // 명시적 버전 지정으로 충돌 해결
    implementation('org.springframework.boot:spring-boot-starter-web') {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
    }
    implementation 'org.springframework.boot:spring-boot-starter-log4j2'
}
```

### 2. 조건부 의존성 추가

```groovy
subprojects {
    dependencies {
        if (project.name == 'database-lib' || project.name.contains('service')) {
            implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
        }
        
        if (project.name.contains('service')) {
            testImplementation 'org.testcontainers:testcontainers:1.19.6'
        }
    }
}
```

### 3. 멀티모듈 테스트 실행

```bash
# 모든 모듈 테스트
./gradlew test

# 특정 모듈만 테스트
./gradlew :saas-api:test

# 특정 테스트 클래스만 실행
./gradlew :saas-api:test --tests '*ApplicationTests'
```

### 4. 멀티모듈 빌드 최적화

```groovy
// build.gradle (루트)
gradle.projectsEvaluated {
    tasks.withType(JavaCompile) {
        options.compilerArgs << '-parameters'
        options.encoding = 'UTF-8'
    }
}
```

### 5. 의존성 그래프 확인

```bash
# 전체 의존성 트리 보기
./gradlew dependencies

# 특정 설정의 의존성만 보기
./gradlew :saas-api:dependencies --configuration runtimeClasspath

# 의존성 리포트 HTML 생성
./gradlew htmlDependencyReport
```

---

## ✅ 체크리스트: 멀티모듈 설정 검증

멀티모듈 프로젝트를 설정할 때 다음을 확인하세요:

### 프로젝트 구조
- [ ] 루트에 settings.gradle이 있고 모든 모듈이 include 됨
- [ ] 각 모듈에 build.gradle이 존재
- [ ] 각 모듈에 src/main/java, src/test/java 구조 존재

### build.gradle 설정
- [ ] 루트 build.gradle에 plugins {} 블록 존재
- [ ] allprojects {} 블록에서 repositories 정의
- [ ] subprojects {} 블록에서 공통 설정 정의
- [ ] 모든 subprojects에 java, java-library 플러그인 적용
- [ ] Spring Boot 플러그인은 apply false 설정

### 모듈별 설정
- [ ] 라이브러리 모듈: bootJar disabled, jar enabled
- [ ] 애플리케이션 모듈: bootJar enabled, jar disabled
- [ ] api() 설정은 java-library 플러그인 적용 후에만 사용 가능
- [ ] 모듈 간 의존성은 project(':모듈명') 형식

### 빌드 및 테스트
- [ ] `./gradlew clean build` 성공
- [ ] `./gradlew test` 성공
- [ ] IDE (IntelliJ)에서 모든 모듈 인식
- [ ] 모듈 간 참조가 빨간 밑줄 없음

### 성능
- [ ] 병렬 빌드 활성화: `org.gradle.parallel=true`
- [ ] 빌드 캐시 활성화: `org.gradle.caching=true`
- [ ] 의존성 캐시 설정 확인

---

## 🚀 고급 설정: 공통 설정 모듈화

큰 프로젝트에서는 buildSrc를 사용하여 설정을 모듈화할 수 있습니다:

**buildSrc/build.gradle**
```groovy
plugins {
    id 'groovy'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation gradleApi()
}
```

**buildSrc/src/main/groovy/shared-configuration.gradle**
```groovy
plugins {
    id 'java'
    id 'java-library'
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

**subproject/build.gradle (간소화)**
```groovy
apply plugin: 'shared-configuration'

dependencies {
    api 'org.springframework.boot:spring-boot-starter-data-jpa'
}
```

---

## 참고 자료

- [Gradle 공식 문서 - Multi-project Builds](https://docs.gradle.org/current/userguide/multi_project_builds.html)
- [Gradle Java Library Plugin](https://docs.gradle.org/current/userguide/java_library_plugin.html)
- [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/)
- [Spring Dependency Management Plugin](https://spring.io/blog/2015/02/03/managing-dependencies-for-multi-module-maven-and-gradle-projects-with-spring-io-platform/)

