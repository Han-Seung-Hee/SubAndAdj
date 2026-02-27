# SubAndAdj 멀티모듈 프로젝트 설정 완료

## 문제 해결 내역

### 1. `prepareKotlinBuildScriptModel` 에러
- **원인**: IntelliJ가 Kotlin DSL 빌드 스크립트를 찾으려 했지만 Groovy DSL을 사용 중
- **해결**: 
  - `gradle.properties` 파일 생성 및 Kotlin 관련 설정 추가
  - IntelliJ IDEA 플러그인을 build.gradle에 추가

### 2. `saas-core` 모듈이 싱크 대상에 포함되지 않음
- **원인**: 
  - `.idea` 폴더가 없어서 IntelliJ가 프로젝트 구조를 올바르게 인식하지 못함
  - 이전에 생성된 IntelliJ 설정 파일 충돌
- **해결**:
  - 모든 캐시 및 이전 설정 파일 삭제 (`.gradle`, `.idea`, `build`)
  - `idea` 플러그인을 Gradle에 추가
  - `.gitignore` 업데이트하여 필요한 IDEA 설정만 제외

## 현재 프로젝트 구조

```
SubAndAdj/
├── build.gradle              # 루트 프로젝트 설정
├── settings.gradle           # 멀티모듈 정의
├── gradle.properties         # Gradle 설정
├── saas-core/               # 공유 라이브러리 모듈
│   ├── build.gradle         # - api 의존성 사용
│   └── src/                 # - bootJar 비활성화 (jar만 생성)
└── saas-api/                # API 애플리케이션 모듈
    ├── build.gradle         # - saas-core 의존
    └── src/                 # - bootJar 활성화 (실행 가능)
```

## 주요 설정

### build.gradle (루트)
- Spring Boot 3.4.1 (Gradle 9.3.1과 호환)
- java-library 플러그인 추가 (api() 메소드 사용을 위해)
- idea 플러그인 추가 (IntelliJ 통합)

### saas-core/build.gradle
- `api 'org.springframework.boot:spring-boot-starter-data-jpa'`
  - api 의존성이므로 saas-api가 자동으로 상속받음
- bootJar 비활성화, plain jar만 생성

### saas-api/build.gradle
- `implementation project(':saas-core')`
  - saas-core의 api 의존성을 모두 사용 가능
- bootJar 활성화 (실행 가능한 Spring Boot 애플리케이션)

## IntelliJ에서 프로젝트 열기

1. IntelliJ IDEA 열기
2. File > Open
3. `SubAndAdj` 폴더 선택
4. "Import Gradle Project"로 열기
5. Gradle 설정:
   - Build and run using: Gradle
   - Run tests using: Gradle
   - Gradle JVM: Java 17 이상

## Gradle 명령어

```bash
# 프로젝트 구조 확인
./gradlew projects

# 전체 빌드 (테스트 제외)
./gradlew build -x test

# 특정 모듈 빌드
./gradlew :saas-core:build
./gradlew :saas-api:build

# 의존성 트리 확인
./gradlew :saas-api:dependencies --configuration compileClasspath

# IntelliJ 프로젝트 파일 재생성 (필요시)
./gradlew cleanIdea idea
```

## 빌드 결과

- **saas-core**: `saas-core-0.0.1-SNAPSHOT-plain.jar` (261 bytes - 라이브러리)
- **saas-api**: `saas-api-0.0.1-SNAPSHOT.jar` (58MB - 실행 가능한 애플리케이션)

## 다음 단계

1. IntelliJ에서 프로젝트를 다시 열기 (File > Open)
2. Gradle 자동 동기화 대기
3. 두 모듈 모두 프로젝트 뷰에서 확인
4. 필요한 엔티티, 리포지토리, 서비스 개발 시작

## 참고사항

- `saas-core`에 공통 엔티티, 리포지토리, 유틸리티 등을 작성
- `saas-api`에서는 컨트롤러와 API 관련 코드만 작성
- 테스트 실행 시 데이터베이스 설정 필요 (현재는 `-x test`로 스킵)

