# RunPassport (Android)

RunPassport의 네이티브 Android 앱. Kotlin + Jetpack Compose로 작성한다.

## 프로젝트 개요

RunPassport는 GPS 기반 러닝 코스 인증 앱이다. 사용자가 정해진 코스를 달리면 위치 궤적을
서버가 등록된 코스와 대조해 완주 여부를 판정하고, 완주 시 스탬프/쿠폰 등을 지급한다.
핵심 기술 난제는 두 가지다: (1) GPS 궤적과 기준 코스를 얼마나 관대하게/엄격하게 대조해서
완주로 인정할지(노이즈가 많은 GPS 신호 처리), (2) 위치 조작·우회 등 부정 완주를 어떻게
막을지다. 백엔드는 Kotlin/Spring Boot + Supabase(Postgres/Auth/RLS)로 별도 저장소에서
개발 중이며, 이 저장소는 그 앱의 Android 클라이언트만 다룬다.

개발자는 이 프로젝트가 첫 Android 프로젝트인 주니어 개발자다. Kotlin/Spring Boot를
실무로 익히는 학습 목적을 겸하고 있으므로, **코드는 개발자 본인이 직접 작성한다.**
Claude Code는 코드를 대신 생성하는 역할이 아니라 설계 상담·리뷰·가이드 역할을 한다
(단, 반복적인 보일러플레이트는 예외로 생성 가능). 그래서 이 문서는 "무엇을 왜 그렇게
해야 하는지"를 규칙마다 이유와 함께 적는다 — 이유를 알아야 다음에 스스로 판단할 수
있기 때문이다.

## 프로젝트 정보

- Package / applicationId: `com.runpassport.app`
- 단일 Gradle 모듈: `:app` (아직 멀티 모듈로 분리하지 않음)
- minSdk 26, targetSdk / compileSdk 37
- AGP 9.3.1, Kotlin 2.2.10, Compose BOM 2026.02.01
- 의존성 버전은 `gradle/libs.versions.toml` (버전 카탈로그)에서 관리
- 아직 git 저장소가 초기화되지 않았음 — `git init`이나 최초 커밋 전에 먼저 물어볼 것
- 현재 코드베이스는 Android Studio 기본 Compose 템플릿 상태다. Hilt, Coroutines/Flow
  명시적 의존성, Room, 네트워킹(Retrofit 등), 위치 관련 코드는 **아직 추가되지 않았다.**
  아래 규칙은 이런 요소가 실제로 도입될 때 따라야 할 방향을 미리 정의해 둔 것이지,
  이미 존재하는 코드를 설명하는 것이 아니다.

## Gradle 커맨드

일관된 출력을 위해 `./gradlew` 직접 호출보다 아래 커맨드를 우선 사용한다.

| 커맨드 | 설명 |
|---|---|
| `/build` | 디버그 APK 빌드 |
| `/test` | 단위 테스트 실행 (기기가 연결돼 있으면 계측 테스트도 함께) |
| `/lint` | Android Lint 실행 후 조치 가능한 항목 요약 |
| `/run` | 연결된 기기/에뮬레이터에 디버그 빌드 설치 후 실행 |
| `/check` | 빌드 + 단위 테스트 + lint 전체 사전 점검 |
| `/clean` | Gradle 빌드 산출물 정리 |

작업을 완료로 볼 때는 항상 `/check`를 먼저 실행한다.

## 구조

```
app/src/main/java/com/runpassport/app/
  MainActivity.kt        진입점
  ui/theme/               Color.kt, Theme.kt, Type.kt (Compose 테마)
app/src/test/             JVM 단위 테스트 (JUnit4)
app/src/androidTest/      계측 테스트 (Espresso + Compose UI test)

ai/
  develop-ticket.md       전체 개발 티켓 목록 (진행 상황의 단일 진실 공급원)
  current-ticket.md       지금 진행 중인 티켓의 상세 (작업 순서, 예시 등)
  response.md             /request 커맨드 결과 저장 위치
  code-review/            /mj-code-review, /ticket-review 결과 저장 위치
  git-pr.md               git PR 메시지
note/
  기능명세.md              노션 기획 문서를 Android 앱 기준으로 재해석한 기능 명세
```

기능이 추가되면 `ui`/`data`/`domain` 같은 레이어 폴더를 루트에 두지 말고,
`com.runpassport.app.<feature>` 형태로 **기능 단위(package by feature)**로 묶는다.
레이어 기준으로 나누면 기능 하나를 고칠 때 파일이 여러 폴더에 흩어져 있어 찾기 어렵고,
기능 삭제 시 흔적이 남기 쉽다.

## 안드로이드 개발 표준 규칙

초심자가 실수하기 쉬운 지점 위주로 정리한다. 이 프로젝트는 위치/코루틴 비중이 높아지므로
해당 항목을 우선순위 상단에 둔다.

### 아키텍처: MVVM + 단방향 데이터 흐름(UDF)

- 화면 상태는 `ViewModel`이 소유하고 `StateFlow`로 노출한다. (`LiveData`는 쓰지 않는다 —
  Compose는 Flow와 자연스럽게 통합되고(`collectAsStateWithLifecycle`), 프로젝트 전체를
  Kotlin/Coroutines 스택으로 통일하는 게 학습 곡선도 하나로 줄어든다.)
- Composable은 상태를 직접 들고 있지 않고 파라미터로 받는다(상태 호이스팅). 그래야 같은
  UI를 Preview에서 테스트하거나 상태 소유자를 바꿔도 UI 코드를 건드릴 필요가 없다.
- 데이터는 항상 한 방향으로 흐른다: `ViewModel`이 상태를 내려보내고, Composable은 이벤트
  (콜백)만 올려보낸다. Composable이 직접 상위 상태를 변경하지 않는다.

### Compose 관례

- Composable 함수명은 명사형 PascalCase (`RunTrackingScreen`, `CourseCard`). 동사형
  이름은 일반 함수와 헷갈린다.
- `remember`는 "이 Composable이 다시 그려져도 값을 유지하고 싶다"는 뜻이지 상태 관리
  도구가 아니다. 여러 화면·ViewModel이 함께 봐야 하는 값은 `remember`가 아니라
  `ViewModel`에 둔다.
- `derivedStateOf`는 매 recomposition마다 다시 계산해도 비용이 크거나, 원본 상태보다
  드물게 바뀌는 파생값에만 쓴다. 단순 계산까지 감싸면 오히려 코드만 복잡해진다.
- 리스트를 그릴 때 `key`를 지정한다(`items(list, key = { it.id })`) — 없으면 리스트 순서가
  바뀔 때 엉뚱한 항목이 recompose되거나 애니메이션이 깨진다.

### 코루틴 / Flow

- 코루틴은 항상 구조화된 스코프 안에서 실행한다. `ViewModel`에서는 `viewModelScope`,
  Composable에서는 `LaunchedEffect`/`rememberCoroutineScope`만 쓴다. `GlobalScope`나
  임의의 `CoroutineScope()`를 직접 만들어 쓰지 않는다 — 화면이 사라져도 코루틴이 안
  끝나서 메모리 누수, 존재하지 않는 화면에 대한 상태 갱신 크래시로 이어진다.
- 코루틴 안 예외는 상위로 던지면 앱이 죽는다. 네트워크/위치 관련 코루틴은 `try/catch`로
  실패를 상태값(예: `UiState.Error`)으로 변환해서 노출한다.
- `suspend` 함수는 자기 자신이 어떤 스레드에서 도는지 신경 쓰지 않게(`Dispatchers`를
  호출부가 아니라 구현부에서 지정) 작성한다. ViewModel에서 `Dispatchers.IO`를 직접
  지정하지 않고, Repository/DataSource 레이어에서 처리한다.

### 위치 / 백그라운드 작업

러닝 트래킹은 이 앱에서 가장 실수하기 쉬운 영역이다.

- 위치 수집이 필요한 화면/서비스는 시작 전 항상 권한을 확인한다
  (`ACCESS_FINE_LOCATION`, 백그라운드 추적이 필요하면 `ACCESS_BACKGROUND_LOCATION`도
  별도 확인 — Android 10+부터 별도 권한이다). 권한 없이 위치 API를 호출하면
  `SecurityException`으로 크래시한다.
- 백그라운드에서 위치를 계속 수집해야 하면 Foreground Service를 쓰고, 알림을 반드시
  띄운다. 트래킹 종료(완주, 사용자 중단, 앱 강제 종료 등 모든 경로)에서 Service를
  확실히 멈추는지 점검한다 — "안 멈추는 Service"는 배터리 소모 민원과 백그라운드
  위치 정책 위반으로 바로 이어진다.
- 트래킹 중 화면 회전, 프로세스 재생성(메모리 부족으로 시스템이 앱을 죽였다 살리는
  경우)에도 진행 중이던 궤적 데이터가 유실되지 않아야 한다. 상태를 Activity/ViewModel
  메모리에만 두지 말고, 필요하면 로컬 저장(추후 Room 도입 예정)을 함께 고려한다.

### 리소스 관리

- 문자열, 색상, 치수 값을 코드에 하드코딩하지 않는다. 문자열은 `strings.xml`, 색상/타이포는
  `ui/theme`(Theme.kt/Color.kt/Type.kt)를 통해서만 참조한다. 다국어 지원이나 다크모드
  전환이 필요해질 때 하드코딩된 값은 전부 다시 찾아 고쳐야 한다.

### Null 안정성

- `!!` (non-null assertion)는 지양한다. `?.`, `?:`, `requireNotNull`, `checkNotNull`을
  우선 사용한다. `!!`는 "여기 절대 null 아님"이라는 확신을 코드가 아니라 개발자 머릿속에만
  담아두는 것이라, 나중에 그 확신이 깨지면 스택트레이스만 남고 원인을 찾기 어렵다.

### 네이밍 컨벤션

- 패키지: 전부 소문자, 기능명 (`com.runpassport.app.tracking`)
- 클래스/Composable/객체: PascalCase (`RunTrackingViewModel`, `CourseCard`)
- 함수/변수: camelCase (`startTracking`, `currentDistanceMeters`)
- 상수: UPPER_SNAKE_CASE, `companion object` 안에 (`MAX_GPS_ACCURACY_METERS`)
- Kotlin 코드 스타일은 `gradle.properties`에 `official`로 고정돼 있다 — 파일 단위로
  덮어쓰지 않는다.

### 커밋 단위

- 기능 단위로 작게 커밋한다. 하나의 커밋에 여러 관심사(예: 트래킹 로직 + UI 스타일링)를
  섞지 않는다 — 리뷰하기도, 나중에 문제 생겼을 때 원인 커밋을 찾기도 어려워진다.
- 빌드가 깨지는 상태로 커밋하지 않는다. 커밋 전 최소한 `/build`가 통과하는지 확인한다.
- 커밋 메시지는 Conventional Commits 형식(`feat:`, `fix:`, `refactor:`, `chore:`, `docs:`)을
  따른다 — 실제 커밋 메시지 생성은 `/commit` 커맨드가 담당한다.

### 의존성 추가

- 새 라이브러리는 `app/build.gradle.kts`에 버전을 직접 적지 않고 `gradle/libs.versions.toml`에
  등록한 뒤 alias로 참조한다. 버전이 한 곳에 모여 있어야 업그레이드할 때 누락되는 곳이
  없다.
- `minSdk`/`targetSdk`/`compileSdk`나 AGP/Kotlin 버전을 바꾸는 것은 사소한 변경이 아니라
  의도적으로 검토해야 할 변경으로 취급한다 (하위 기기 지원 범위, 다른 라이브러리와의
  호환성에 영향).

## 티켓/작업 관리 규칙

`ai/develop-ticket.md`가 진행 상황의 단일 진실 공급원(source of truth)이다. 새 작업을
시작하기 전에 항상 이 파일을 먼저 읽는다.

1. **DONE된 티켓은 삭제한다.** develop-ticket.md는 "지금부터 할 일" 목록이지 히스토리
   기록이 아니다 — 히스토리는 git log가 대신한다.
2. **티켓 하나 = 눈으로 확인 가능한 완료 조건 단위.** "코스 목록 화면 만들기"처럼 크게
   잡지 않고, 실행해서 눈으로 통과/실패를 확인할 수 있는 크기로 쪼갠다.
3. **순서: 개발 → 검증(실행/로그 확인) → 통과 → 상태 갱신 → 다음 티켓.** 검증 없이
   상태를 먼저 바꾸지 않는다.
4. **앞 티켓이 DONE 되기 전 다음 티켓 코드를 작성하지 않는다.** 병렬로 진행하면 어느
   변경이 어느 티켓 때문인지 diff에서 구분이 안 된다.
5. **코드는 개발자 본인이 직접 작성한다.** Claude Code는 설계/리뷰만 한다 (단순
   보일러플레이트 생성은 예외).
6. **현재 진행 중인 티켓의 상세(작업 순서, 예시 등)는 `ai/current-ticket.md`에 적는다.**
   develop-ticket.md는 전체 목록이라 한 티켓의 세부 계획까지 담으면 목록이 너무 길어진다.
7. **티켓을 DONE 처리하고 목록에서 삭제하기 전, 완료 메모 전문(배운 점·의사결정·스코프
   변경 사유)을 `ai/ticket-archive.md`에 시간순으로 옮겨 남긴다.** Notion "Run패스포트
   티켓 아카이브" DB에도 같은 내용을 요약해서 페이지로 등록하고, `티켓` 속성으로 원본
   Notion 티켓과 관계형으로 연결한다 (`레포` 속성은 이 저장소 기준 `runpassport-android`로
   설정). 1번 규칙대로 DONE 티켓을 삭제하면 "왜 이렇게 했는지"가 함께 사라지는데, 그
   기록을 로컬(전문)과 Notion(요약 + 원본 티켓 관계형 링크)에 나눠 보존하기 위함이다.

## 주니어 개발자를 위한 PR 전 체크리스트

- [ ] 하드코딩된 문자열/매직 넘버가 있는가
- [ ] 위치 권한이 없는 상태에서 크래시 나지 않는가 (권한 거부/취소 경로 확인)
- [ ] 코루틴이 화면 회전/프로세스 재생성에도 안전한가 (`viewModelScope` 밖에서 도는
      코루틴이 없는가)
- [ ] 네트워크/위치 실패 시 UI가 무한 로딩에 빠지지 않고 에러 상태를 보여주는가
- [ ] `!!` 사용을 다른 null 처리로 바꿀 수 있는 곳은 없는가
- [ ] 새로 추가한 문자열/색상이 `strings.xml`/테마를 거치는가
- [ ] Foreground Service를 새로 건드렸다면, 모든 종료 경로에서 Service가 멈추는가
- [ ] 커밋이 기능 단위로 쪼개져 있고 각 커밋에서 빌드가 깨지지 않는가
- [ ] `/check` (빌드 + 단위 테스트 + lint)가 통과하는가
- [ ] `ai/develop-ticket.md`/`ai/current-ticket.md`의 완료 조건을 실제로 충족했는가

## 코드 리뷰 기준

`/mj-code-review`, `/ticket-review` 두 커맨드가 공통으로 따르는 기준이다.

- 심각도 3단계로 지적한다.
  - 🔴 **심각** — 크래시, 보안(권한/데이터), 데이터 정합성(완주 판정 로직 오류 등)에
    영향을 주는 문제. 반드시 고치고 넘어가야 한다.
  - 🟡 **개선 권장** — 컨벤션 위반, 가독성, 잠재적 버그이지만 지금 당장 크래시로
    이어지지는 않는 것 (예: `!!` 사용, 상태 호이스팅 안 된 Composable).
  - 🟢 **사소함/제안** — 스타일, 더 코틀린다운 표현 등 있으면 좋은 것.
- 위치는 `파일경로:라인번호` 형식으로 구체적으로 짚는다. "이 부분이 이상하다"가 아니라
  "왜 문제인지"와 "어떻게 고치면 되는지"를 함께 적는다.
- 코드를 대신 고치지 않고 지적만 한다 — 고치는 것은 항상 개발자 본인 몫이다(학습 목적).

## Claude Code 커맨드 안내

실제 동작은 각 `.claude/commands/*.md` 파일에 정의돼 있고, 여기서는 색인 역할만 한다.

| 커맨드 | 용도 |
|---|---|
| `/commit` | 변경사항(git diff) 기반으로 Conventional Commits 형식 커밋 메시지 생성 |
| `/create-pr` | 현재 브랜치와 main의 차이를 분석해 GitHub PR 본문 작성 |
| `/mj-code-review` | 현재 변경사항(diff)을 위 코드 리뷰 기준으로 리뷰 |
| `/request` | 임의의 질문/요청 처리, 결과는 `ai/response.md`에 기록 |
| `/sync-ticket` | 노션 'Run패스포트 마일스톤'/'Run패스포트 Develop' DB(담당 영역=프론트엔드)와 `ai/develop-ticket.md`를 대조해 불일치 리포트, 반영은 건별 확인 후 처리 |
| `/ticket-review` | `ai/current-ticket.md`의 완료 조건 대비 구현 코드를 리뷰, 결과는 `ai/code-review/`에 기록 |

## 참고 문서 위치

- `note/기능명세.md` — 노션 기획 문서를 Android 앱 구현 관점으로 재해석한 기능 명세
- `ai/develop-ticket.md` — 전체 개발 티켓 목록 (source of truth)
- `ai/current-ticket.md` — 현재 진행 중인 티켓 상세
- `ai/ticket-archive.md` — DONE 처리 후 삭제된 티켓의 완료 메모 전문 (시간순)
- `ai/code-review/` — `/mj-code-review`, `/ticket-review` 결과 저장 위치
- `ai/response.md` — `/request` 커맨드 결과 저장 위치
- `ai/git-pr.md` — `/create-pr` 결과의 클립보드 복사 실패 시 대체 저장 위치

## Testing

- 단위 테스트: JUnit4, `app/src/test` 하위.
- 계측/UI 테스트: Espresso + `androidx.compose.ui:ui-test-junit4`, `app/src/androidTest`
  하위 (연결된 기기/에뮬레이터 필요).
- 작업을 끝내기 전 항상 `/check`를 실행한다.
