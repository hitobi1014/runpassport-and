# ticket-archive.md

> `develop-ticket.md` 진행 규칙상 DONE된 티켓은 목록에서 삭제된다. 삭제되기 전 완료 메모(배운 점,
> 의사결정, 스코프 변경 사유)를 여기 시간순으로 남긴다.
> Android 전용 — **이 저장소(runpassport-android)가 원본(전문)**. 백엔드/인프라 쪽 완료 기록은
> 해당 저장소(runpassport-api)의 `ai/ticket-archive.md`에 따로 관리된다.
> Notion "Run패스포트 티켓 아카이브" DB에도 같은 내용을 짧게 남기고 원본 Notion 티켓과 관계형으로
> 연결해둔다 (`레포=runpassport-android`로 필터). 여기는 그 로컬 원본(전문) — Notion 쪽은 요약만
> 담고 상세 기록은 항상 이 파일을 기준으로 본다.

---

## 2026-08-19 — [W1] Android 프로젝트 셋업 (Kotlin, Compose, Hilt, Room, Retrofit)

- 노션 링크: https://app.notion.com/p/3b49c9e2d47881ef8589f7ecac7db7f2
- 완료 조건: 앱이 실기기에서 빌드/실행됨 / Hilt DI 그래프가 정상 동작함

### 목표

Kotlin + Jetpack Compose 기본 템플릿 상태였던 프로젝트에 Hilt/Room/Retrofit 의존성을
설치하고 Hilt DI 그래프를 구성해, 이후 티켓들이 의존할 수 있는 프로젝트 기반을 완성한다.

### 완료 메모

- `gradle/libs.versions.toml`에 `ksp`/`hilt`/`room`/`retrofit`/`okhttp` 버전과 관련
  라이브러리·플러그인을 등록하고, `app/build.gradle.kts`에서 실제 적용함.
- Hilt 진입점(`RunPassportApplication` + `@HiltAndroidApp`)과 DI 그래프 동작 확인용 더미
  클래스(`HiltSmokeTestRepository`)를 작성해 `MainActivity`에서 주입받아 화면에 표시하는
  방식으로 실기기 빌드/실행과 DI 그래프 동작을 함께 검증함.
- 진행 중 Gradle Sync 에러 2건을 순서대로 해결함:
  1. Hilt 2.57.1이 AGP 9.3.1과 호환되지 않아(`BaseExtension`을 못 찾음) 2.60.1로 상향.
  2. KSP가 Kotlin 버전과 짝지어지는 옛날 버전 체계(`2.2.10-2.0.2`)라 AGP 9의 "빌트인
     코틀린" 방식과 충돌 → 코틀린 버전과 무관한 독립 버전 체계인 `2.3.9`로 상향.
  3. Hilt 2.57.1이 Java 17을 요구해 `compileOptions`와 `kotlin { jvmToolchain(17) }`을
     11→17로 함께 상향함 (Java/Kotlin 버전을 따로 맞추면 "jvm target compatibility"
     에러가 남).
- 실기기 실행 시 "Hilt 연결됨" 문구가 정상적으로 표시되어 완료 조건(빌드/실행, DI 그래프
  동작) 모두 확인됨. 검증용으로만 쓰인 `HiltSmokeTestRepository`와 `MainActivity`의 관련
  주입 코드는 이후 실제 기능 개발 시 자연스럽게 대체될 예정이라 이번 완료 처리와 별개로
  남겨둠(제거는 후속 작업에서 자연히 처리됨).

### 참고

- 상세 논의: `ai/response.md` ("Hilt/Room/Retrofit 라이브러리 설치 + Hilt 셋업 + 더미 검증
  클래스" 절) 참고.
- Notion "Run패스포트 티켓 아카이브" DB에도 같은 내용을 요약해 페이지로 등록함(`레포`=
  `runpassport-android`, `티켓`은 위 노션 링크로 관계형 연결). 상세 기록은 이 파일을
  기준으로 본다.
