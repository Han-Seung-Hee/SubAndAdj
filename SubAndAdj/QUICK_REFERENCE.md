# Gradle 멀티모듈 빠른 설정 체크리스트

## 🚀 5분 안에 멀티모듈 설정하기

### Step 1: 프로젝트 생성 (1분)

```bash
# 루트 디렉토리 생성
mkdir MyMultimoduleProject
cd MyMultimoduleProject

# git 초기화 (선택)
git init
```

### Step 2: settings.gradle 작성 (30초)

```groovy
rootProject.name = 'MyProject'
include 'module-a'
include 'module-b'
include 'module-c'
```

**체크:**
- [ ] rootProject.name 설정됨
- [ ] 모든 모듈이 include 됨
- [ ] 모듈명이 실제 디렉토리명과 일치

### Step 3: 루트 build.gradle 작성 (2분)

**복사 & 붙여넣기:**

```groovy
plugins {
    id 'org.springframework.boot' version '3.4.1' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}

allprojects {
    group = 'com.example'
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

    dependencies {
        compileOnly 'org.projectlombok:lombok'
        annotationProcessor 'org.projectlombok:lombok'
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    }

    tasks.named('test') { useJUnitPlatform() }
}
```

**체크:**
- [ ] plugins 블록 포함
- [ ] allprojects 블록 포함
- [ ] subprojects 블록 포함
- [ ] java + java-library 플러그인 포함

### Step 4: 모듈 디렉토리 생성 (1분)

```bash
# 각 모듈별 디렉토리 구조 생성
mkdir -p module-a/src/main/java/com/example/modulea
mkdir -p module-a/src/test/java/com/example/modulea
mkdir -p module-a/src/main/resources

mkdir -p module-b/src/main/java/com/example/moduleb
mkdir -p module-b/src/test/java/com/example/moduleb
mkdir -p module-b/src/main/resources

# 더미 파일 생성 (IDE가 인식하도록)
touch module-a/src/main/java/com/example/modulea/.gitkeep
touch module-b/src/main/java/com/example/moduleb/.gitkeep
```

### Step 5: 각 모듈별 build.gradle 작성 (1분)

**module-a/build.gradle (라이브러리)**
```groovy
dependencies {
    api 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.postgresql:postgresql'
}

bootJar { enabled = false }
jar { enabled = true }
```

**module-b/build.gradle (애플리케이션)**
```groovy
dependencies {
    implementation project(':module-a')
    implementation 'org.springframework.boot:spring-boot-starter-web'
}

bootJar { enabled = true }
jar { enabled = false }
```

**체크:**
- [ ] 라이브러리 모듈: api 사용 + bootJar disabled
- [ ] 애플리케이션 모듈: implementation project() + bootJar enabled
- [ ] 모든 모듈에 build.gradle 있음

### Step 6: 빌드 테스트 (30초)

```bash
# 빌드 실행
./gradlew clean build

# 성공 확인
# BUILD SUCCESSFUL in Xs 메시지 보임
```

**체크:**
- [ ] 빌드 성공 (BUILD SUCCESSFUL)
- [ ] 모든 모듈이 컴파일됨
- [ ] JAR 파일 생성됨 (build/libs/)

---

## ✅ 의존성 설정 빠른 가이드

### 의존성 유형별 사용법

```groovy
// 1. 외부 라이브러리 (가장 흔함)
implementation 'org.springframework.boot:spring-boot-starter-web'

// 2. 외부 라이브러리 (다른 모듈도 사용해야 할 때)
api 'org.springframework.boot:spring-boot-starter-data-jpa'

// 3. 같은 프로젝트의 다른 모듈
implementation project(':module-name')

// 4. 테스트 전용
testImplementation 'org.springframework.boot:spring-boot-starter-test'

// 5. 특정 configuration만
runtimeOnly 'org.postgresql:postgresql'
compileOnly 'org.projectlombok:lombok'
```

### 언제 api()를 써야 할까?

**api() 사용해야 함:**
```groovy
// ✅ 다른 모듈이 이 클래스를 사용하는 경우
public class UserEntity { // JPA Entity
    // public 클래스
}

api 'org.springframework.boot:spring-boot-starter-data-jpa'
// 다른 모듈도 JPA 사용 필요
```

**implementation 사용:**
```groovy
// ❌ 내부 구현만 필요한 경우
implementation 'org.postgresql:postgresql'
// 다른 모듈은 PostgreSQL을 직접 사용하지 않음
```

---

## 🔍 자주 하는 실수와 해결법

### 실수 1: api() 설정 사용했는데 에러

```
❌ Could not find method api()
```

**원인:** java-library 플러그인 없음

**해결:**
```groovy
// ✅ build.gradle에 추가
apply plugin: 'java-library'  // ← 이거 추가!
```

### 실수 2: 모듈을 만들었는데 인식 안됨

```
❌ Could not find project ':new-module'
```

**원인:** settings.gradle에 include 안함

**해결:**
```groovy
// ✅ settings.gradle에 추가
include 'new-module'
```

### 실수 3: 라이브러리 모듈에서 bootJar 비활성화 안함

```
❌ 불필요한 bootJar 생성 (saas-core-0.0.1-SNAPSHOT.jar)
```

**원인:** JAR 설정 누락

**해결:**
```groovy
// ✅ 라이브러리 모듈의 build.gradle
bootJar { enabled = false }
jar { enabled = true }
```

### 실수 4: 모듈 간 순환 의존성

```
❌ Module A → B → A (순환!)
```

**원인:** 잘못된 설계

**해결:**
```
# ✅ 단방향 의존성 구조
common-library (의존 없음)
    ↑
    ├─ module-a
    └─ module-b
        ↑
        └─ api-module
```

### 실수 5: IDE에서 모듈이 인식 안됨

```
❌ 빨간 밑줄 표시, 모듈 목록에 없음
```

**원인:** IDE 캐시

**해결:**
```bash
# IntelliJ IDEA
# 1. File → Invalidate Caches
# 2. Invalidate and Restart 클릭
# 3. Gradle 탭 → Refresh 버튼

# 터미널
./gradlew clean build
```

---

## 📋 배포 전 확인 체크리스트

### 빌드 검증

```bash
# 1. 전체 빌드
./gradlew clean build
# 결과: BUILD SUCCESSFUL

# 2. 의존성 트리 확인
./gradlew dependencies
# 모든 의존성이 resolve됨

# 3. 특정 모듈만 테스트
./gradlew :module-a:test
# 테스트 성공

# 4. 실행 가능한 JAR 확인
ls build/libs/module-b-*.jar
# module-b-1.0.0.jar 파일 존재 (bootJar)
```

### 코드 검증

```bash
# 5. 라이브러리 모듈 검증
# - bootJar disabled?
grep "bootJar { enabled = false }" module-a/build.gradle
# - jar enabled?
grep "jar { enabled = true }" module-a/build.gradle

# 6. 애플리케이션 모듈 검증
# - bootJar enabled?
grep "bootJar { enabled = true }" module-b/build.gradle
# - jar disabled?
grep "jar { enabled = false }" module-b/build.gradle
```

### 최종 확인

- [ ] 모든 모듈이 settings.gradle에 include됨
- [ ] 모든 모듈에 build.gradle이 있음
- [ ] 라이브러리: bootJar disabled, jar enabled
- [ ] 애플리케이션: bootJar enabled, jar disabled
- [ ] api()와 implementation 올바르게 구분
- [ ] 모든 빌드 성공
- [ ] 모든 테스트 통과
- [ ] IDE에서 모든 모듈 인식

---

## 🎯 실행 가능한 JAR 배포

### 빌드

```bash
./gradlew :module-b:bootJar
# 생성: module-b/build/libs/module-b-1.0.0.jar
```

### 실행

```bash
java -jar module-b/build/libs/module-b-1.0.0.jar

# 포트 변경
java -jar module-b/build/libs/module-b-1.0.0.jar --server.port=8081
```

### JAR 내용 확인

```bash
jar -tf module-b/build/libs/module-b-1.0.0.jar | head -20

# 또는
unzip -l module-b/build/libs/module-b-1.0.0.jar | head -20
```

---

## 🚨 트러블슈팅 명령어

### 1. Gradle 데몬 초기화

```bash
# 데몬 중지
./gradlew --stop

# 다시 빌드 (새 데몬 시작)
./gradlew clean build
```

### 2. 캐시 삭제

```bash
# Gradle 캐시 삭제
rm -rf ~/.gradle/caches

# 프로젝트 빌드 폴더 삭제
./gradlew clean
```

### 3. 의존성 문제 진단

```bash
# 자세한 로그 출력
./gradlew build --debug 2>&1 | grep -i error

# 의존성 의존 분석
./gradlew dependencyInsight --dependency org.springframework --configuration compile
```

### 4. IDE 설정 재생성

```bash
# IntelliJ IDEA 설정 재생성
./gradlew idea

# Eclipse 설정 재생성
./gradlew eclipse

# 빌드 캐시 초기화
./gradlew cleanBuildCache
```

---

## 📚 파일 템플릿

### gradle.properties (선택사항)

```properties
# JDK 버전
java.version=17

# Spring 버전
springBootVersion=3.4.1
springDependencyManagementVersion=1.1.7

# 성능 최적화
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.workers.max=8

# 개발 환경
org.gradle.jvmargs=-Xmx2G -XX:MaxMetaspaceSize=512m
```

### .gitignore

```
build/
.gradle/
.idea/
*.iml
*.class
*.jar
.DS_Store
```

---

## 핵심 요점 정리

| 항목 | 중요 설정 |
|------|---------|
| **settings.gradle** | `include 'module-name'` |
| **루트 build.gradle** | `java-library` 플러그인, `subprojects {}` |
| **라이브러리 모듈** | `api`, `bootJar { enabled = false }` |
| **애플리케이션 모듈** | `implementation project()`, `bootJar { enabled = true }` |
| **버전 관리** | `apply false`로 플러그인 버전 관리 |
| **테스트** | `useJUnitPlatform()` 설정 |

---

## 다음 스텝

이 체크리스트를 따라 설정한 후:

1. ✅ `./gradlew build` 성공
2. ✅ IDE에서 모든 모듈 인식
3. ✅ 모듈 간 의존성 작동

그 다음:

→ 프로젝트 특화 설정 (데이터베이스, 보안, 프로파일 등)
→ CI/CD 파이프라인 구축
→ 성능 최적화 및 모니터링

**추가 문서 참조:**
- GRADLE_MULTIMODULE_GUIDE.md - 상세 가이드
- GRADLE_MULTIMODULE_EXAMPLES.md - 고급 예제
- PROJECT_BUILD_ANALYSIS.md - 현 프로젝트 분석

