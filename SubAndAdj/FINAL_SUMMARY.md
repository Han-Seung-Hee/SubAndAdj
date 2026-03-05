# 📋 Gradle 멀티모듈 완전 가이드 - 최종 요약

## 🎯 프로젝트 상태

현재 SubAndAdj 프로젝트는 **멀티모듈 Gradle 빌드 시스템으로 정상 구성**되었습니다.

```bash
✅ ./gradlew clean build  # 성공적으로 빌드됨
```

---

## 📚 생성된 문서 목록

### 📖 4개의 상세 가이드 문서

| # | 문서명 | 내용 | 읽는 시간 | 추천 순서 |
|---|--------|------|----------|---------|
| 1️⃣ | **QUICK_REFERENCE.md** | 5분 설정 가이드, 실수 해결, 명령어 | 5-10분 | **첫 번째** |
| 2️⃣ | **GRADLE_MULTIMODULE_GUIDE.md** | 기본 개념, 에러 분석, 모범 사례 | 15-20분 | **두 번째** |
| 3️⃣ | **PROJECT_BUILD_ANALYSIS.md** | 현 프로젝트 라인별 분석, 흐름도 | 20-30분 | **세 번째** |
| 4️⃣ | **GRADLE_MULTIMODULE_EXAMPLES.md** | 4가지 프로젝트 구조 예제 | 20분 | **네 번째** |

### 🗺️ 가이드 네비게이션 문서

- **README_GRADLE_GUIDE.md** - 모든 문서의 네비게이션 및 학습 경로

---

## 🔧 해결된 문제

### 원래 에러
```
Could not find method api() for arguments 
[org.springframework.boot:spring-boot-starter-data-jpa] 
on object of type org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler.
```

### 원인
- `saas-core`에서 `api()` 설정을 사용했는데
- `java-library` 플러그인이 적용되지 않았음

### 해결
- ✅ 루트 `build.gradle`의 `subprojects`에 `apply plugin: 'java-library'` 추가
- ✅ `saas-core`에서 `api()` 설정 정상 작동

---

## 📊 현재 프로젝트 구조

```
SubAndAdj (루트)
│
├── saas-core/ (라이브러리 모듈)
│   ├── build.gradle
│   │   └─ api 'org.springframework.boot:spring-boot-starter-data-jpa'
│   │   └─ implementation 'org.postgresql:postgresql'
│   │   └─ bootJar { enabled = false }  ← 실행 안 함
│   │   └─ jar { enabled = true }       ← 라이브러리로만 제공
│   └── src/
│
├── saas-api/ (애플리케이션 모듈)
│   ├── build.gradle
│   │   └─ implementation project(':saas-core')
│   │   └─ implementation 'org.springframework.boot:spring-boot-starter-web'
│   │   └─ bootJar { enabled = true }   ← 실행 가능한 JAR 생성
│   │   └─ jar { enabled = false }
│   └── src/
│
└── build.gradle (루트 - 공통 설정)
    ├─ plugins { ... apply false }
    ├─ allprojects { ... }
    └─ subprojects {
        ├─ apply plugin: 'java'
        ├─ apply plugin: 'java-library'  ← 핵심!
        ├─ apply plugin: 'org.springframework.boot'
        └─ apply plugin: 'io.spring.dependency-management'
```

---

## 🎓 핵심 학습 요점

### 1. 의존성 전파 원리

```
saas-api의 관점:

① project(':saas-core')를 의존하면
   saas-core의 모든 소스 코드에 접근 가능

② saas-core에서 api로 정의된 의존성
   → saas-api도 자동으로 사용 가능
   
③ saas-core에서 implementation으로 정의된 의존성
   → saas-api는 사용 불가 (내부 세부사항)

구체적 예시:
┌─────────────────────────────────────────┐
│ saas-core/build.gradle                  │
├─────────────────────────────────────────┤
│ api 'spring-boot-starter-data-jpa'      │
│ implementation 'postgresql:postgresql'  │
└─────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│ saas-api/build.gradle                   │
├─────────────────────────────────────────┤
│ implementation project(':saas-core')    │
└─────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│ saas-api에서 사용 가능                  │
├─────────────────────────────────────────┤
│ ✅ JPA API (api로 전파됨)               │
│ ❌ PostgreSQL (구현 세부사항)           │
└─────────────────────────────────────────┘
```

### 2. api vs implementation 선택 기준

```
의사결정 트리:

다른 모듈이 이 의존성을 알아야 하는가?
├─ YES (공개 API 클래스에서 반환)
│   └─→ api 'org.springframework.boot:spring-boot-starter-data-jpa'
│       예: JPA Entity, Repository 인터페이스
│
└─ NO (내부 구현만 필요)
    └─→ implementation 'org.postgresql:postgresql'
        예: 데이터베이스 드라이버, 내부 유틸리티
```

### 3. bootJar vs jar

```
Spring Boot 플러그인의 선택:

이 모듈이 독립적으로 실행되는가?
├─ YES (최종 애플리케이션)
│   └─→ bootJar { enabled = true }
│       jar { enabled = false }
│       생성: saas-api-x.x.x.jar (실행 가능)
│
└─ NO (라이브러리)
    └─→ bootJar { enabled = false }
        jar { enabled = true }
        생성: saas-core-x.x.x.jar (라이브러리)
```

### 4. 플러그인 구성의 의미

```
subprojects {
    apply plugin: 'java'
    # ↑ 기본: 소스 컴파일, 테스트, JAR 패킹
    
    apply plugin: 'java-library'
    # ↑ 추가: api 설정 지원 (다른 모듈에 공개할 API 정의)
    
    apply plugin: 'org.springframework.boot'
    # ↑ Spring Boot 기능: 자동 설정, 내장 톰캣, bootJar 생성
    
    apply plugin: 'io.spring.dependency-management'
    # ↑ 버전 관리: Spring 라이브러리 버전 자동 해결
}
```

---

## ⚡ 자주 사용하는 명령어

### 빌드 명령어

```bash
# 전체 빌드
./gradlew clean build

# 특정 모듈만 빌드
./gradlew :saas-api:build
./gradlew :saas-core:build

# 테스트만 실행
./gradlew test
./gradlew :saas-api:test

# 의존성 트리 확인
./gradlew dependencies
./gradlew :saas-api:dependencies
```

### 의존성 관리

```bash
# 의존성 버전 확인
./gradlew :saas-api:dependencies --configuration runtimeClasspath

# 특정 라이브러리 찾기
./gradlew dependencyInsight --dependency postgresql
```

### 실행 가능한 JAR 빌드 & 실행

```bash
# JAR 빌드
./gradlew :saas-api:bootJar

# JAR 실행
java -jar saas-api/build/libs/saas-api-0.0.1-SNAPSHOT.jar

# 포트 변경하여 실행
java -jar saas-api/build/libs/saas-api-0.0.1-SNAPSHOT.jar --server.port=8081
```

### 디버깅 & 문제 해결

```bash
# Gradle 데몬 중지 & 재시작
./gradlew --stop
./gradlew clean build

# 자세한 로그
./gradlew build --debug

# IDE 설정 재생성
./gradlew idea  # IntelliJ
./gradlew eclipse  # Eclipse
```

---

## 🎯 체크리스트: 현재 상태 확인

### ✅ 프로젝트 설정

- [x] `settings.gradle`에 모든 모듈 include됨
- [x] 루트 `build.gradle`에 `java-library` 플러그인 적용
- [x] 모든 서브프로젝트에 공통 설정 정의
- [x] `saas-core`: 라이브러리 설정 완료
- [x] `saas-api`: 애플리케이션 설정 완료

### ✅ 의존성 설정

- [x] `saas-core`의 공개 API를 `api()`로 정의
- [x] `saas-core`의 내부 구현을 `implementation()`으로 정의
- [x] `saas-api`가 `project(':saas-core')`에 의존
- [x] 의존성 계층 명확함

### ✅ JAR 설정

- [x] `saas-core`: `bootJar disabled`, `jar enabled`
- [x] `saas-api`: `bootJar enabled`, `jar disabled`
- [x] 실행 가능한 JAR 생성 가능

### ✅ 빌드 & 테스트

- [x] `./gradlew clean build` 성공
- [x] 모든 모듈 컴파일 성공
- [x] IDE에서 모든 모듈 인식

---

## 📈 다음 단계

### 단계 1: 이 가이드로 학습 (지금)
**추천 읽기 순서:**
1. QUICK_REFERENCE.md (5분)
2. GRADLE_MULTIMODULE_GUIDE.md (20분)
3. PROJECT_BUILD_ANALYSIS.md (30분)

### 단계 2: 프로젝트 이해 & 실습
```bash
# 각 문서를 읽으면서 직접 빌드 및 모듈 수정 시도
./gradlew :saas-core:build  # 라이브러리 빌드 확인
./gradlew :saas-api:bootJar # 애플리케이션 JAR 생성
java -jar saas-api/build/libs/saas-api-0.0.1-SNAPSHOT.jar  # 실행
```

### 단계 3: 구조 확장 (필요시)
- 새 모듈 추가 (QUICK_REFERENCE.md 참조)
- gradle.properties 도입 (GRADLE_MULTIMODULE_EXAMPLES.md 참조)
- 마이크로서비스 구조로 변경 (GRADLE_MULTIMODULE_EXAMPLES.md 예제 2 참조)

### 단계 4: 고급 기능 (선택)
- buildSrc로 설정 모듈화 (GRADLE_MULTIMODULE_EXAMPLES.md 참조)
- CI/CD 파이프라인 구축
- 성능 최적화 (병렬 빌드, 캐싱)

---

## 🚀 실전 팁

### Tip 1: 모듈 추가 시 체크리스트

```bash
# 1. 디렉토리 생성
mkdir new-module
mkdir -p new-module/src/main/java/com/example
mkdir -p new-module/src/test/java

# 2. build.gradle 생성
touch new-module/build.gradle

# 3. settings.gradle에 추가
echo "include 'new-module'" >> settings.gradle

# 4. 빌드 테스트
./gradlew clean build
```

### Tip 2: 의존성 충돌 해결

```groovy
// implementation에서 버전 명시
implementation('org.springframework.boot:spring-boot-starter-web') {
    // 필요시 exclude 설정
    exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
}

// 또는 명시적 버전 지정
implementation 'com.google.guava:guava:33.0.0-jre'
```

### Tip 3: gradle.properties 도입하기

```properties
# gradle.properties
springBootVersion=3.4.1
postgresqlVersion=42.7.0
```

```groovy
// build.gradle
plugins {
    id 'org.springframework.boot' version "${springBootVersion}" apply false
}
```

### Tip 4: IDE 캐시 문제 해결

```bash
# IntelliJ IDEA
File → Invalidate Caches... → Invalidate and Restart

# 또는 터미널
./gradlew clean build
rm -rf .gradle  # 극단적인 경우
```

---

## 🎓 학습 완료 기준

이 가이드를 다 읽고 실습한 후:

1. ✅ 멀티모듈 프로젝트의 장점과 단점을 설명할 수 있다
2. ✅ settings.gradle과 build.gradle의 역할을 설명할 수 있다
3. ✅ api와 implementation을 상황에 맞게 사용할 수 있다
4. ✅ 새로운 모듈을 추가할 수 있다
5. ✅ 멀티모듈 프로젝트를 빌드하고 배포할 수 있다
6. ✅ 발생하는 에러를 자가 진단하고 해결할 수 있다
7. ✅ 다른 개발자에게 설명할 수 있다

**이 모든 항목에 체크가 되면 마스터입니다!** 🎉

---

## 📞 빠른 참조

### "api()를 어디서 사용하지?"
→ QUICK_REFERENCE.md의 "의존성 유형별 사용법" 섹션

### "현재 build.gradle이 뭐하는 건지 모르겠는데?"
→ PROJECT_BUILD_ANALYSIS.md의 "루트 build.gradle 분석" 섹션

### "새 모듈 어떻게 추가하지?"
→ QUICK_REFERENCE.md의 "Step 4: 모듈 디렉토리 생성"

### "마이크로서비스 구조로 만들고 싶은데?"
→ GRADLE_MULTIMODULE_EXAMPLES.md의 "예제 2: 3단계 구조"

### "에러가 났는데 뭐가 문제야?"
→ QUICK_REFERENCE.md의 "자주 하는 실수와 해결법" 또는
   GRADLE_MULTIMODULE_GUIDE.md의 "일반적인 문제 해결"

---

## 📚 추가 학습 자료

### 공식 문서
- [Gradle 공식: 멀티프로젝트 빌드](https://docs.gradle.org/current/userguide/multi_project_builds.html)
- [Gradle 공식: Java Library Plugin](https://docs.gradle.org/current/userguide/java_library_plugin.html)
- [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/)

### 실습 프로젝트
- Spring Boot 공식 예제: https://github.com/spring-projects/spring-boot/tree/main/spring-boot-samples
- Gradle 멀티모듈 예제: https://github.com/gradle/gradle-in-action-source

---

## 💡 최종 조언

> **멀티모듈은 처음에는 복잡해 보이지만, 한 번 이해하면 매우 강력합니다.**

### 성공의 비결
1. **단계적 학습**: 한 번에 다 이해하려고 하지 말 것
2. **직접 실습**: 문서만 읽지 말고 직접 해보기
3. **에러 환영**: 에러를 통해 가장 깊이 있게 배움
4. **문서 활용**: 같은 에러가 다시 나면 문서에서 찾기

---

**지금부터 시작하세요!** 🚀

첫 번째 읽을 문서: **QUICK_REFERENCE.md** (5분)

Happy coding! 🎉

