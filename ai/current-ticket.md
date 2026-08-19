# 현재 작업 티켓: 사전 생성 계정 로그인 (Supabase Email/Password 콤보박스 선택)

- 노션 링크: https://app.notion.com/p/3b49c9e2d4788166a1d0c0a2b1ea694d
- 소속 마일스톤: W1 · 프로젝트 셋업 & 외부 API 확보 (2026-08-06 ~ 2026-08-12)
- 담당 영역: 프론트엔드
- 선행 티켓: [AND] Android 프로젝트 셋업 (Kotlin, Compose, Hilt, Room, Retrofit) — 완료
  (아카이브됨, `ai/ticket-archive.md` 2026-08-19 항목 참고)

## 결정 배경 (스코프 변경, 2026-08-19)

이 프로젝트는 관광공사 공모전 제출용 데모이고, 로그인은 핵심 기능이 아니다. 원래 카카오/구글
등 OAuth 연동을 검토했으나 제외하기로 결정했다.

- **이유**: 공모전 데모는 실사용자 유입 시나리오가 아니라 심사 시연이 목적이다. OAuth 콘솔
  등록, 동의항목 심사, 딥링크 콜백 디버깅 같은 비용이 GPS 인증/스탬프 같은 핵심 기능 구현
  시간을 깎아먹는다.
- **아키텍처 영향 없음**: Supabase Auth의 `auth.uid()` 기반 RLS 정책은 로그인 방식과 무관하게
  동일하게 동작하므로, 실서비스 전환 시 OAuth를 다시 붙이더라도 이 결정이 백엔드/DB
  아키텍처에 영향을 주지 않는다.
- **대신 확정된 방식**: Supabase Auth 이메일/비밀번호 계정을 미리 5~10개 생성해두고, 로그인
  화면에 텍스트 입력 폼 대신 계정 목록을 콤보박스(드롭다운)로 보여줘 선택 시 해당 계정으로
  로그인 처리한다. 회원가입/비밀번호 찾기/이메일 인증은 구현하지 않는다.

나중에 "왜 OAuth를 안 썼는지" 다시 물어볼 상황을 대비해 이 절을 남겨둔다. (기존 카카오 SDK
연동 계획 전문은 git 이력의 이전 버전 `ai/current-ticket.md` 참고.)

## 목표

로그인 화면의 콤보박스에서 사전 생성된 계정 하나를 선택하면 Supabase Auth 세션(JWT)이
발급되어 로그인 상태가 되고, 앱을 완전히 종료했다가 재실행해도 그 로그인 상태가 유지된다.

## 완료 조건 (Definition of Done)

- [ ] 사전 생성 계정 목록이 로그인 화면 콤보박스에 노출된다
- [ ] 계정 선택 후 로그인 시 Supabase 세션(JWT)이 정상 발급된다
- [ ] 발급된 세션으로 백엔드 API 호출이 인증된다 (Authorization 헤더에 JWT 포함)
- [ ] 앱을 재실행해도 로컬에 저장된 세션으로 로그인 상태가 복원된다 (자동 로그아웃되지 않음)
- [ ] 로그인 실패 시 무한 로딩 없이 에러 상태를 사용자에게 보여준다

## 구현 순서

1. Supabase 대시보드(또는 Admin API)에서 이메일/비밀번호 테스트 계정 5~10개를 사전
   생성(seed)한다. 계정 목록(이메일)은 앱에 하드코딩하지 않고 별도 관리 방식을 검토한다
   (아래 "미확정 사항" 참고).
2. `gradle/libs.versions.toml`에 Supabase Kotlin 클라이언트(Auth 모듈) 버전을 등록한 뒤
   `app/build.gradle.kts`에 alias로 반영한다.
3. `com.runpassport.app.auth` 패키지를 생성한다 (package by feature 컨벤션).
4. `AuthRepository` 작성: 사전 생성 계정 이메일 목록을 제공하고, 선택된 이메일로
   `signInWith(Email)`을 호출해 세션을 반환하는 역할을 담당한다.
5. 세션(access/refresh token) 로컬 저장을 구현한다. Room이 아니라 `DataStore(Preferences)`
   또는 `EncryptedSharedPreferences`를 사용한다 (민감정보라 평문 저장 지양).
6. `AuthViewModel` 작성: 계정 콤보박스 선택 → 로그인(4) → 세션 저장(5)까지의 플로우를
   `StateFlow<AuthUiState>`(Idle/Loading/Success/Error)로 노출한다.
7. `LoginScreen` Composable 작성: 계정 콤보박스(드롭다운) + 로그인 버튼 + 로딩/에러 상태
   표시. `AuthViewModel`의 상태를 파라미터로 받는 상태 호이스팅 구조로 작성한다.
8. 앱 시작 지점(`MainActivity` 또는 별도 스플래시)에서 5번에 저장된 세션이 있으면 로그인
   화면을 건너뛰고 홈으로 이동하는 세션 복원 로직을 추가한다.

## 파일별 작업 내역

| 파일 경로 | 작업 내용 | 신규/수정 |
|---|---|---|
| `gradle/libs.versions.toml` | Supabase 클라이언트 의존성 버전 등록 | 수정 |
| `app/build.gradle.kts` | 위 의존성 alias 추가 | 수정 |
| `app/src/main/java/com/runpassport/app/auth/AuthRepository.kt` | 계정 목록 제공 + Supabase 이메일 로그인 + 세션 저장 | 신규 |
| `app/src/main/java/com/runpassport/app/auth/AuthViewModel.kt` | 로그인 플로우 상태 관리 (`StateFlow`) | 신규 |
| `app/src/main/java/com/runpassport/app/auth/LoginScreen.kt` | 계정 콤보박스 로그인 화면 Composable | 신규 |

## 참고할 기존 코드

참고할 기존 패턴 없음 — 신규 구조. 프로젝트가 아직 기본 Compose 템플릿 상태라
ViewModel/Repository 패턴 자체가 이 티켓에서 처음 도입된다. `MainActivity.kt`의
`RunpassportTheme { Scaffold { ... } }` 진입 구조 정도만 그대로 따르면 된다.

## API 연동 (해당되는 경우)

Supabase Auth의 이메일/비밀번호 로그인은 클라이언트 SDK에서 직접 호출하므로(카카오 토큰
교환처럼 자체 백엔드 엔드포인트가 필요 없음), 기존에 막혀 있던 "백엔드 Auth 스펙 미확인"
이슈가 이번 스코프 변경으로 해소된다. 로그인 이후 백엔드 API를 호출할 때는 Supabase가
발급한 JWT를 `Authorization: Bearer <token>` 헤더로 전달하면 되고, 백엔드는 이 JWT를 검증해
`auth.uid()` 기반 RLS를 적용한다 — 기존 Supabase Auth 구조를 그대로 쓰는 부분이라 별도 확인이
필요 없다.

## 주의사항 / 흔히 하는 실수

- Supabase 로그인 호출을 `viewModelScope` 밖에서 launch하지 않는다 (CLAUDE.md 코루틴 규칙).
- 로그인 실패(잘못된 계정 상태, 네트워크 오류 등) 시 무한 로딩에 빠지지 않고 에러 상태를
  보여준다 (CLAUDE.md PR 전 체크리스트 항목).
- 세션 값에 `!!` 사용을 지양한다 — 로그인 실패 시 null 처리를 명확히 한다.
- Supabase URL/키, 계정 비밀번호를 코드에 하드코딩하지 않는다.
- 화면 회전/프로세스 재생성이 일어나도 로그인 진행 중(로딩) 상태가 깨지지 않는지 확인한다.

## 테스트/검증 방법

- 콤보박스에서 계정을 선택해 로그인 → 세션 발급 확인.
- 로그인 성공 후 앱을 완전히 종료(프로세스 kill)했다가 재실행해 로그인 화면을 건너뛰고
  홈으로 바로 진입하는지 확인 (세션 복원 검증).
- 발급된 세션으로 백엔드 API를 실제로 호출해 인증이 통과하는지 확인.
- 잘못된/만료된 세션 상태에서 에러 처리가 무한 로딩 없이 되는지 확인.

## 미확정 사항

1. 사전 생성 계정의 이메일/비밀번호 목록을 앱에서 어떻게 관리할지 — 코드 하드코딩 대신
   `BuildConfig` 필드나 별도 설정 파일 등을 검토해야 한다 (이 프로젝트에 아직 시크릿 관리
   컨벤션이 없음).
2. 계정 생성(seed)을 누가/어떻게 하는지 — Supabase 대시보드에서 수동 생성할지, 백엔드 쪽
   시딩 스크립트로 할지는 백엔드 저장소(`runpassport-api`)와 협의가 필요하다.
3. 로그아웃 기능이 이번 티켓 스코프에 포함되는지 — 완료 조건에는 없지만, 세션 유지를
   제대로 검증하려면 사실상 필요할 수 있다.

---

**질문**: 미확정 사항 1~2(계정 시딩 방식·주체)는 백엔드 쪽과 맞춰야 하는 부분이라, 착수 전에
방향을 확인해주시면 좋겠습니다.
