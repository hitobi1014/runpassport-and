# 현재 작업 티켓: 카카오 로그인 SDK 연동 및 토큰 → Supabase 세션 교환

- 노션 링크: https://app.notion.com/p/3b49c9e2d4788166a1d0c0a2b1ea694d
- 소속 마일스톤: W1 · 프로젝트 셋업 & 외부 API 확보 (2026-08-06 ~ 2026-08-12)
- 담당 영역: 프론트엔드
- 선행 티켓: [AND] Android 프로젝트 셋업 (Kotlin, Compose, Hilt, Room, Retrofit) — **진행중(미완료)**.
  사용자 확인 하에 병행 진행하기로 함. 셋업 티켓에서 Hilt/Retrofit 기본 구성이 아직 없을 수
  있으므로, 이 티켓에서 필요한 의존성/DI 모듈이 없다면 이 범위 안에서 최소한으로 먼저 추가한다.

## 목표

카카오 로그인 버튼을 눌러 인증을 완료하면 앱이 Supabase 세션(access/refresh token)을
보유한 로그인 상태가 되고, 앱을 완전히 종료했다가 재실행해도 그 로그인 상태가 유지된다.

## 완료 조건 (Definition of Done)

- [ ] 카카오 로그인 SDK가 연동되어 있고, 로그인 버튼을 누르면 카카오 인증 플로우가 실행된다
- [ ] 실기기에서 카카오 계정으로 로그인이 성공한다 (티켓 원문 완료 조건)
- [ ] 발급된 카카오 액세스 토큰이 서버로 전달되어 Supabase Auth 세션으로 교환된다
- [ ] Supabase 세션이 생성되고 유지된다 (티켓 원문 완료 조건)
- [ ] 앱을 재실행해도 로컬에 저장된 세션으로 로그인 상태가 복원된다 (자동 로그아웃되지 않음)
- [ ] 로그인 실패/취소 시 무한 로딩 없이 에러 상태를 사용자에게 보여준다

## 구현 순서

1. `gradle/libs.versions.toml`에 카카오 로그인 SDK(`com.kakao.sdk:v2-user`)와 네트워킹
   의존성(Retrofit/OkHttp — 아직 없다면 이 범위에서 최소 추가) 버전 등록 후
   `app/build.gradle.kts`에 alias 반영.
2. 카카오 개발자 콘솔에서 발급받은 네이티브 앱 키를 `AndroidManifest.xml`의 카카오 로그인
   리다이렉트용 커스텀 스킴 액티비티에 등록. 앱 키 자체는 코드에 하드코딩하지 않고
   `local.properties` → `BuildConfig` 필드로 노출하는 방식을 검토 (컨벤션 미확정, 아래
   "미확정 사항" 참고).
3. `com.runpassport.app.auth` 패키지 생성 (package by feature 컨벤션).
4. `KakaoAuthDataSource` 작성: 카카오 SDK `UserApiClient.instance.loginWithKakaoAccount(...)`
   콜백 API를 `suspendCancellableCoroutine`으로 감싸 suspend 함수로 노출. 카카오 액세스
   토큰(String)을 반환.
5. `AuthRepository` 작성: 3번에서 받은 카카오 토큰을 백엔드(또는 Supabase)에 전달해 세션을
   받아오는 역할. **백엔드 엔드포인트 스펙이 아직 없으므로**, 인터페이스로 분리하고
   Mock/Real 두 구현으로 나눈다 — 스펙 확정 전에도 화면/플로우 개발이 막히지 않게 하기
   위함. (2번 항목은 4번의 카카오 토큰이 있어야 호출 가능 — 순서 의존)
6. 세션(access/refresh token) 로컬 저장 구현. Room이 아니라 `DataStore(Preferences)` 또는
   `EncryptedSharedPreferences` 사용 (민감정보라 평문 저장 지양).
7. `AuthViewModel` 작성: 로그인 버튼 클릭 → 카카오 로그인(4) → 토큰 교환(5) → 세션
   저장(6)까지의 플로우를 `StateFlow<AuthUiState>`(Idle/Loading/Success/Error)로 노출.
8. `LoginScreen` Composable 작성: 로그인 버튼과 로딩/에러 상태 표시. `AuthViewModel`의
   상태를 파라미터로 받는 상태 호이스팅 구조로 작성.
9. 앱 시작 지점(`MainActivity` 또는 별도 스플래시)에서 6번에 저장된 세션이 있으면 로그인
   화면을 건너뛰고 홈으로 이동하는 세션 복원 로직 추가.

## 파일별 작업 내역

| 파일 경로 | 작업 내용 | 신규/수정 |
|---|---|---|
| `gradle/libs.versions.toml` | 카카오 로그인 SDK, 네트워킹 의존성 버전 등록 | 수정 |
| `app/build.gradle.kts` | 위 의존성 alias 추가 | 수정 |
| `app/src/main/AndroidManifest.xml` | 카카오 로그인 리다이렉트 액티비티/커스텀 스킴 등록 | 수정 |
| `app/src/main/java/com/runpassport/app/auth/KakaoAuthDataSource.kt` | 카카오 SDK 로그인 호출 wrapper (suspend) | 신규 |
| `app/src/main/java/com/runpassport/app/auth/AuthRepository.kt` | 토큰 교환 + 세션 저장 담당 (인터페이스 + Mock/Real) | 신규 |
| `app/src/main/java/com/runpassport/app/auth/AuthViewModel.kt` | 로그인 플로우 상태 관리 (`StateFlow`) | 신규 |
| `app/src/main/java/com/runpassport/app/auth/LoginScreen.kt` | 로그인 화면 Composable | 신규 |
| `app/src/main/res/values/strings.xml` | 카카오 관련 문자열 리소스화 (앱 키 자체는 별도 검토) | 수정 |

## 참고할 기존 코드

참고할 기존 패턴 없음 — 신규 구조. 프로젝트가 아직 기본 Compose 템플릿 상태라
ViewModel/Repository 패턴 자체가 이 티켓에서 처음 도입된다. `MainActivity.kt`의
`RunpassportTheme { Scaffold { ... } }` 진입 구조 정도만 그대로 따르면 된다.

## API 연동 (해당되는 경우)

**스펙 미확인.** 백엔드 저장소(`runpassport-api`)를 확인했으나 카카오 토큰 교환 관련
컨트롤러/엔드포인트가 아직 없다 — `auth/SecurityConfig.kt`는 Spring Security를 stateless로
설정해둔 골격뿐이고, JWT 발급/검증 로직이나 `/auth/kakao` 류의 엔드포인트가 없다. 노션 티켓
본문에도 "서버 측 Auth 설정과 병행 확인 필요"라고만 적혀 있고 구체 스펙은 없다.

확인 방법: 백엔드 담당자(또는 `runpassport-api` 쪽 관련 티켓 진행 시 본인)에게 아래를
확정받아야 한다.
1. 앱이 카카오 액세스 토큰을 보낼 자체 백엔드 엔드포인트가 있는지, 아니면 Supabase Auth의
   카카오 OIDC 연동을 앱에서 직접 쓰는 구조인지
2. 세션 응답 형태 (access_token/refresh_token/만료시간 등 필드명과 타입)

## 주의사항 / 흔히 하는 실수

- 카카오 SDK 콜백 API를 코루틴으로 감쌀 때 `viewModelScope` 밖에서 launch하지 않는다
  (CLAUDE.md 코루틴 규칙).
- 로그인 실패/취소(사용자가 카카오 인증 중 뒤로가기 등) 시 무한 로딩에 빠지지 않고 에러
  상태를 보여준다 (CLAUDE.md PR 전 체크리스트 항목).
- 토큰/세션 값에 `!!` 사용을 지양한다 — 토큰 교환 실패 시 null 처리를 명확히 한다.
- 카카오 앱 키, Supabase URL/키를 코드에 하드코딩하지 않는다.
- 화면 회전/프로세스 재생성이 일어나도 로그인 진행 중(로딩) 상태가 깨지지 않는지 확인한다.

## 테스트/검증 방법

- 실기기(카카오 로그인은 에뮬레이터에서 제약이 있을 수 있어 가능하면 실기기)에서 실제
  카카오 계정으로 로그인 시도.
- 로그인 성공 후 앱을 완전히 종료(프로세스 kill)했다가 재실행해 로그인 화면을 건너뛰고
  홈으로 바로 진입하는지 확인 (세션 복원 검증).
- 로그인 중 취소/뒤로가기 시나리오에서 크래시나 무한 로딩이 없는지 확인.
- 백엔드 스펙이 아직 없으므로, `AuthRepository`는 Mock 구현으로 우선 위 시나리오를
  검증하고, 실제 스펙이 나오면 Real 구현으로 교체한다.

## 미확정 사항

1. 카카오 토큰 → Supabase 세션 교환을 자체 백엔드 엔드포인트로 할지, Supabase의 카카오
   OIDC 연동을 앱에서 직접 쓸지 — 백엔드 스펙이 아직 없어 확정 불가.
2. 로그아웃 기능이 이번 티켓 스코프에 포함되는지 — 완료 조건에는 없지만, 세션 유지를
   제대로 검증하려면 사실상 필요할 수 있음.
3. 카카오 앱 키 / Supabase 프로젝트 키를 로컬에서 어떻게 관리할지 — 이 프로젝트에 아직
   시크릿 관리 컨벤션이 없음 (`local.properties` + `BuildConfig` 필드 노출 방식 등 검토 필요).

---

**질문**: 위 미확정 사항 1~3에 대해 방향을 알려주시면 계획을 더 구체화하겠습니다. 특히
1번(백엔드 연동 방식)은 구현 순서 5번(`AuthRepository`)의 실제 모양을 좌우하는 부분이라,
착수 전에 확인되면 가장 좋습니다.
