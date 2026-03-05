# 🎓 Gradle 멀티모듈 프로젝트 - 완전 가이드

SubAndAdj 프로젝트의 Gradle 멀티모듈 설정과 관련된 모든 문서입니다.

## 📚 문서 구성

### 1. 📖 **GRADLE_MULTIMODULE_GUIDE.md** (필독)
**대상:** Gradle 멀티모듈을 처음 배우는 사람

주요 내용:
- 멀티모듈의 개념과 구조 설명
- 에러 원인 분석 및 해결법
- api vs implementation 상세 설명
- 일반적인 문제 해결 (FAQ)
- 체크리스트

**언제 읽어야 함:**
- 멀티모듈 프로젝트 설정할 때 (정독)
- "Could not find method api()" 에러 발생 시 (해결법 섹션)
- 의존성 설정이 헷갈릴 때 (api vs implementation 섹션)

**읽는 시간:** 15-20분

---

### 2. 🔍 **PROJECT_BUILD_ANALYSIS.md** (상황 파악용)
**대상:** 현재 SubAndAdj 프로젝트를 깊이 있게 이해하고 싶은 사람

주요 내용:
- 현재 프로젝트의 build.gradle 라인별 상세 분석
- 설정의 논리적 흐름 설명
- 의존성 전파 흐름 시각화
- 빌드 결과물 분석
- 발생했던 에러의 원인 상세 분석
- 향후 개선 제안

**언제 읽어야 함:**
- 현재 프로젝트 구조 완전히 이해하고 싶을 때
- 왜 이렇게 설정했는지 알고 싶을 때
- 다른 프로젝트에 똑같은 패턴 적용할 때

**읽는 시간:** 20-30분

---

### 3. 💡 **GRADLE_MULTIMODULE_EXAMPLES.md** (실습용)
**대상:** 다양한 프로젝트 구조를 보고 자신의 프로젝트에 맞는 패턴 찾는 사람

주요 내용:
- 예제 1: 기본 2단계 구조 (현재 프로젝트와 동일)
- 예제 2: 3단계 구조 (마이크로서비스)
- 예제 3: 전문 라이브러리 구조
- 예제 4: gradle.properties를 통한 버전 관리
- 실전 팁 (의존성 버전 충돌, 조건부 의존성 등)
- buildSrc를 이용한 고급 설정

**언제 읽어야 함:**
- 새로운 멀티모듈 프로젝트 시작할 때
- 프로젝트 구조를 어떻게 정할지 고민할 때
- gradle.properties 도입하고 싶을 때
- 고급 기능 활용하고 싶을 때

**읽는 시간:** 20분 (필요한 예제만 선택해서)

---

### 4. ⚡ **QUICK_REFERENCE.md** (빠른 참조)
**대상:** 5분 안에 멀티모듈 설정해야 하는 사람 / 자주 헷갈리는 부분만 확인하는 사람

주요 내용:
- 5분 안에 멀티모듈 설정하기 (Step 1-6)
- 의존성 설정 빠른 가이드
- 자주 하는 실수와 해결법
- 배포 전 확인 체크리스트
- 트러블슈팅 명령어
- 핵심 요점 정리 (표로 정리)

**언제 읽어야 함:**
- 급할 때 (5분 내에 확인)
- 의존성 타입을 잊어버렸을 때
- 흔한 에러가 뭐였는지 확인하고 싶을 때
- 배포 전 최종 체크

**읽는 시간:** 5-10분

---

## 🎯 상황별 읽기 가이드

### 상황 1: "Gradle 멀티모듈이 뭐예요?"
**추천 순서:**
1. GRADLE_MULTIMODULE_GUIDE.md (전체)
2. GRADLE_MULTIMODULE_EXAMPLES.md (예제 1)
3. QUICK_REFERENCE.md (Step 1-6)

**총 시간:** 40분

---

### 상황 2: "Could not find method api() 에러가 났어요"
**추천 순서:**
1. QUICK_REFERENCE.md ("실수 1" 섹션)
2. GRADLE_MULTIMODULE_GUIDE.md ("설정 방법" 섹션)
3. PROJECT_BUILD_ANALYSIS.md ("현재 프로젝트 분석")

**총 시간:** 15분

---

### 상황 3: "현재 프로젝트의 build.gradle이 뭔가요?"
**추천 순서:**
1. PROJECT_BUILD_ANALYSIS.md (처음부터 끝까지)
2. 이해 안 되는 부분은 GRADLE_MULTIMODULE_GUIDE.md에서 검색

**총 시간:** 30분

---

### 상황 4: "새 프로젝트에 멀티모듈 구조 적용하고 싶어요"
**추천 순서:**
1. QUICK_REFERENCE.md (Step 1-6)
2. GRADLE_MULTIMODULE_EXAMPLES.md (적절한 예제 선택)
3. 필요하면 GRADLE_MULTIMODULE_GUIDE.md에서 상세 정보 확인

**총 시간:** 30-45분

---

### 상황 5: "마이크로서비스 구조로 멀티모듈 설계하고 싶어요"
**추천 순서:**
1. GRADLE_MULTIMODULE_EXAMPLES.md (예제 2, 3, 4)
2. GRADLE_MULTIMODULE_GUIDE.md (모범 사례 섹션)
3. PROJECT_BUILD_ANALYSIS.md (현재 설정과 비교)

**총 시간:** 45분

---

## 📊 빠른 비교: 문서별 특징

```
┌──────────────────────┬──────────┬────────────┬──────────┐
│ 문서명                │ 길이     │ 상세도     │ 실습성   │
├──────────────────────┼──────────┼────────────┼──────────┤
│ QUICK_REFERENCE      │ ⭐⭐    │ ⭐       │ ⭐⭐⭐⭐ │
│ GUIDE                │ ⭐⭐⭐  │ ⭐⭐⭐   │ ⭐⭐⭐  │
│ PROJECT_ANALYSIS     │ ⭐⭐⭐⭐│ ⭐⭐⭐⭐ │ ⭐⭐    │
│ EXAMPLES             │ ⭐⭐⭐⭐│ ⭐⭐⭐   │ ⭐⭐⭐⭐ │
└──────────────────────┴──────────┴────────────┴──────────┘
```

---

## 🚀 처음 사용자를 위한 "이 문서부터 읽으세요"

### 첫 번째: QUICK_REFERENCE.md

왜?
- 가장 짧음 (5-10분)
- 최소한의 개념만 설명
- 실제 코드 예제 많음
- 대부분의 질문에 답변 포함

### 두 번째: GRADLE_MULTIMODULE_GUIDE.md

왜?
- 멀티모듈 개념을 쉽게 설명
- 에러가 발생한 이유 명확함
- api vs implementation 상세 설명
- 체크리스트로 확인 가능

### 세 번째: PROJECT_BUILD_ANALYSIS.md

왜?
- 현재 프로젝트 완전히 이해
- 다른 프로젝트에 응용 가능
- 왜 이렇게 설정했는지 알게 됨

### 네 번째: GRADLE_MULTIMODULE_EXAMPLES.md

왜?
- 다양한 구조 학습
- 프로젝트 성장에 대비
- 고급 기능 습득

---

## 🎯 핵심 학습 목표

### 이 가이드를 다 읽은 후 알아야 할 것

1. ✅ Gradle 멀티모듈의 장점과 구조
2. ✅ settings.gradle의 역할
3. ✅ build.gradle의 allprojects vs subprojects
4. ✅ api vs implementation 언제 어떻게 사용
5. ✅ 라이브러리 vs 애플리케이션 모듈 설정 차이
6. ✅ bootJar vs jar의 차이
7. ✅ java-library 플러그인이 필요한 이유
8. ✅ 의존성 전파의 흐름
9. ✅ 흔한 에러와 해결법
10. ✅ 멀티모듈 빌드 명령어

---

## 🔗 문서 간의 관계도

```
시작 (5분)
    ↓
QUICK_REFERENCE
    ↓
기본 개념 이해 (20분)
    ↓
GRADLE_MULTIMODULE_GUIDE
    ↓
    ├─ 현재 프로젝트 이해 → PROJECT_BUILD_ANALYSIS
    │
    └─ 다른 구조 배우기 → GRADLE_MULTIMODULE_EXAMPLES
                    ↓
            고급 기능 학습
```

---

## 💾 오프라인 읽기

모든 문서는 마크다운 형식(.md)이므로:

### 로컬에서 읽기
```bash
# 브라우저에서 보기
open QUICK_REFERENCE.md

# 터미널에서 읽기
cat GRADLE_MULTIMODULE_GUIDE.md | less

# 에디터로 열기
code GRADLE_MULTIMODULE_GUIDE.md
```

### PDF로 변환
```bash
# brew install pandoc 필요
pandoc GRADLE_MULTIMODULE_GUIDE.md -o guide.pdf

# Mac의 기본 마크다운 뷰어
mdls GRADLE_MULTIMODULE_GUIDE.md
```

---

## 📞 문서별 목차

### QUICK_REFERENCE.md
- 5분 안에 멀티모듈 설정하기
- 의존성 설정 빠른 가이드
- 자주 하는 실수와 해결법
- 배포 전 확인 체크리스트
- 트러블슈팅 명령어

### GRADLE_MULTIMODULE_GUIDE.md
- 프로젝트 구조
- 에러 원인 분석
- 설정 방법 (4가지 파일 상세 설명)
- 모범 사례
- 일반적인 문제 해결
- 체크리스트
- 추가 리소스

### PROJECT_BUILD_ANALYSIS.md
- 프로젝트 구조 분석
- settings.gradle 라인별 분석
- 루트 build.gradle 상세 분석
- 각 모듈의 build.gradle 분석
- 의존성 전파 흐름 (시각화)
- 빌드 결과물 분석
- 문제 해결 가이드
- 현재 설정의 강점 및 개선 제안

### GRADLE_MULTIMODULE_EXAMPLES.md
- 예제 1: 기본 2단계 구조
- 예제 2: 3단계 구조 (마이크로서비스)
- 예제 3: 라이브러리 전문 구조
- 예제 4: gradle.properties 버전 관리
- 실전 팁 (4가지)
- 체크리스트
- 고급 설정: buildSrc 활용

---

## 📌 주요 개념 빠른 정리

### api vs implementation

```
의존성이 다른 모듈에 필요한가?
  ├─ Yes → api
  └─ No  → implementation
```

### bootJar vs jar

```
실행 가능한 Spring Boot 애플리케이션?
  ├─ Yes → bootJar enabled, jar disabled
  └─ No  → bootJar disabled, jar enabled
```

### 플러그인 구성

```
모든 서브프로젝트에 필요:
  ├─ java (컴파일, 패킹)
  ├─ java-library (api 설정 지원)
  ├─ org.springframework.boot (스프링 부트 기능)
  └─ io.spring.dependency-management (버전 관리)
```

---

## 🎓 다음 학습 주제

이 가이드를 마친 후 권장 학습:

1. **Git 멀티모듈 관리**
   - monorepo vs polyrepo 전략
   - 모듈별 브랜치 관리

2. **CI/CD 파이프라인**
   - 모듈별 독립 배포
   - 의존성 기반 빌드 순서

3. **성능 최적화**
   - 병렬 빌드 설정
   - 캐시 전략

4. **고급 Gradle 기능**
   - �스텀 플러그인 개발
   - buildSrc 활용

5. **마이크로서비스 설계**
   - 모듈 간 독립성
   - 이벤트 기반 통신

---

## ✨ 최종 체크리스트

이 가이드를 다 읽은 후:

- [ ] 멀티모듈 프로젝트 정의 가능
- [ ] 현재 SubAndAdj 프로젝트 완벽히 이해
- [ ] api vs implementation 구분 가능
- [ ] 새 모듈 추가할 때 설정 가능
- [ ] 에러 발생 시 자가 진단 가능
- [ ] 다른 사람에게 설명 가능

**축하합니다!** 이 모든 것을 할 수 있다면 Gradle 멀티모듈 마스터입니다. 🎉

---

**마지막 팁:** 문서를 전부 읽기보다는, 필요한 부분부터 읽고 실습하면서 배우는 것이 더 효과적입니다. 실패와 에러를 통해 배우는 것이 가장 좋은 학습 방법입니다!

**Happy Coding!** 🚀

