# 요청 응답: Notion 티켓 동기화 관리 방식

## 요청

backend 저장소(`runpassport-api`)의 `.claude/commands/sync-ticket.md`가 "Notion 조회 결과를
로컬 `develop-ticket.md`에 곧바로 반영"하는 방식에서 "읽기 전용 대조 리포트만 만들고, 실제
반영(로컬 수정/Notion 상태 갱신)은 항목별로 사용자 승인을 받은 뒤 1건씩 처리"하는 방식으로
바뀌었음. 같은 Notion 데이터베이스를 공유하는 이 저장소(Android)의 `/sync-ticket`도 동일한
설계로 맞춰달라는 요청.

## 배경 — 자동 반영에서 대조 리포트로 바꾼 이유

기존 `sync-ticket.md`는 노션 조회 결과를 사람 확인 없이 곧바로 `ai/develop-ticket.md`에 써
넣는 구조였다. 이 방식은

- Notion 티켓 1개를 로컬에서 2~3개 세부 개발 항목으로 쪼개는 과정에서 사람이 개입하지
  않으면 세분화 기준이 실행할 때마다 달라질 수 있고,
- 로컬에서 이미 체크되거나 진행 중으로 표시된 상태를 노션 스냅샷으로 덮어써버릴 위험이
  있으며,
- "DONE 티켓은 로컬에서 삭제한다"는 진행 규칙과 맞물려, 자동 반영이 실수로 진행 중인 작업
  기록을 지울 수 있다.

그래서 동기화의 "조회/대조"와 "실제 반영"을 분리해, 반영은 항상 사람이 1건씩 확인하고
승인한 것만 처리하도록 설계를 바꿨다. `develop-ticket.md`/`ticket-archive.md`의 기존
원칙(자동 삭제 금지, DONE 티켓은 로컬에서 지우되 완료 메모는 남김)과도 더 잘 맞는다.

## 이번에 한 것

- **`.claude/commands/request.md` 복구**: 다른 내용이 실수로 이 파일에 덧붙여져 frontmatter
  블록이 두 번 나오는 상태였음(파일 파싱이 깨질 수 있는 상태) → 원래의 범용 `/request`
  절차로 되돌림.
- **`.claude/commands/sync-ticket.md` 재작성**: 백엔드 저장소의 "대조 리포트 + 건별 승인"
  설계를 이 저장소에 맞게 적용. 백엔드 버전과의 주요 차이점:
  - 백엔드는 로컬 티켓 제목의 `(#12)` / `(#10 하위)` 표기로 Notion ID를 매칭하지만, 이
    저장소의 로컬 티켓(`ai/develop-ticket.md`)은 처음부터 `- 노션 링크: <URL>` 필드를 갖고
    있어 그 URL을 매칭 키로 썼다 (제목 표기 방식 자체가 다름).
  - 로컬 상태 값이 영어(TODO/IN_PROGRESS/BLOCKED/DONE)가 아니라 한국어(시작전/진행중,
    완료는 삭제)라서 매핑표를 이 저장소의 실제 값 기준으로 다시 작성.
  - 담당 영역 필터를 '백엔드'/'인프라' → '프론트엔드'로 변경 (이 저장소는 프론트엔드=Android
    앱 담당 티켓만 다룸).
  - 완료 티켓을 Notion 아카이브 DB에 반영할 때 `레포` 속성을 `runpassport-android`로 고정.
  - Notion 데이터소스(Develop/마일스톤/아카이브 컬렉션 ID)는 백엔드와 동일한 워크스페이스를
    그대로 재사용 (요청대로 "노션 데이터베이스는 동일하게 사용").
- **`CLAUDE.md`의 커맨드 색인 표** `/sync-ticket` 설명을 "동기화" → "대조 리포트, 반영은
  건별 확인 후 처리"로 갱신.

## 확인이 필요합니다

- 담당 영역 속성명·옵션값이 실제로 여전히 `영역` / `프론트엔드`인지: 이전 `/sync-ticket`
  실행 때 확인된 값을 그대로 가정했다. 노션 스키마가 바뀌었으면 새 `/sync-ticket`도 1단계에서
  스키마부터 다시 확인하도록 되어 있으니 실행 시 자동으로 걸러진다.
- 실제 `/sync-ticket` 실행은 아직 하지 않음 (이번 요청 범위는 커맨드 파일 자체 재작성까지) —
  다음 실행 때 새 대조 리포트 형식이 실제로 잘 동작하는지 확인이 필요하다.

---

# Hilt/Room/Retrofit 라이브러리 설치 + Hilt 셋업 + 더미 검증 클래스

## 2026-08-19 업데이트 — Gradle Sync 에러 수정 (Hilt 버전)

Gradle Sync 시 아래 에러가 발생함:
```
An exception occurred applying plugin request [id: 'com.google.dagger.hilt.android', version: '2.57.1']
> Failed to apply plugin 'com.google.dagger.hilt.android'.
   > Android BaseExtension not found.
```

**원인**: 처음 넣은 Hilt 2.57.1이 이 프로젝트의 AGP 9.3.1과 실제로는 호환되지 않았음.
찾아보니 Hilt Gradle 플러그인의 AGP 9 지원 이력이 복잡했다 — 2.57.1은 AGP 9의 새 DSL
구조에서 `BaseExtension`(AGP 8 이하의 옛날 확장 타입)을 못 찾아 이 에러를 냄. 2.59에서
AGP 9 지원이 추가됐다고 발표됐지만, 그 버전은 `ComponentTreeDeps`라는 런타임 클래스가
아티팩트에서 누락된 별도의 치명적 버그가 있었음. **2.60.1에서 그 버그까지 수정되어 실제로
AGP 9와 정상 동작한다.**

**수정**: `gradle/libs.versions.toml`의 `hilt` 버전을 `2.57.1` → `2.60.1`로 변경함. 다른
파일은 그대로. **Android Studio에서 다시 Gradle Sync 눌러주면 됨.**

만약 이번에도 에러가 나면 그 에러 메시지도 그대로 붙여넣어주면 계속 확인하겠음 (AGP 9는
아직 생태계 전반의 호환성이 계속 다듬어지는 중이라, Room/KSP/Retrofit 쪽에서도 비슷한
문제가 생길 가능성이 있음 — 하나씩 나오는 대로 고치면 됨).

## 2026-08-19 업데이트 2 — Gradle Sync 에러 수정 (KSP 버전 · AGP 9 빌트인 코틀린)

두 번째 Sync에서 새 에러 발생:
```
Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin.
Kotlin source set 'debug' contains: [.../build/generated/ksp/debug/kotlin, .../build/generated/ksp/debug/java]
Solution: Use android.sourceSets DSL instead.
```

**원인**: AGP 9는 "빌트인 코틀린(built-in Kotlin)"이라는 새 방식을 쓴다 — 예전처럼 별도
`org.jetbrains.kotlin.android` 플러그인 없이 AGP 자체가 코틀린 컴파일을 직접 처리한다
(이 프로젝트에 애초에 그 플러그인이 없던 이유가 이거였음). 그런데 처음에 넣은 KSP
`2.2.10-2.0.2`는 코틀린 버전에 맞춰 짝을 이루던 옛날 버전 체계라, 내부적으로 옛날 방식
(`kotlin.sourceSets`)으로 생성된 소스 경로를 등록하는데, 이게 AGP 9의 빌트인 코틀린과
충돌해서 이 에러가 남.

**수정**: KSP는 최근 코틀린 버전과 무관한 **독립 버전 체계**로 바뀌었음 (공식 문서 확인:
예시가 Kotlin 2.4.10 + KSP 2.3.10 조합으로 나옴 — 더 이상 `kotlin버전-ksp버전` 형식이
아님). `gradle/libs.versions.toml`의 `ksp` 버전을 `2.2.10-2.0.2` → **`2.3.9`**로 변경함
(Maven Central에 실제로 배포돼 있는 것까지 확인). 이 버전대(2.3.6+)부터 AGP 9 빌트인
코틀린 인식 방식이 개선됐다고 되어 있어 이 에러가 해결될 것으로 보임.

다시 Gradle Sync 눌러주면 됨. 혹시 이번에도 안 되면, AGP가 공식적으로 제공하는 임시
우회 옵션도 있음 (`gradle.properties`에 `android.disallowKotlinSourceSets=false` 추가) —
근본 수정이 안 먹히면 이걸로 일단 빌드는 통과시키고 넘어갈 수 있음.

---

## 2026-08-19 업데이트 3 — 현재 상태 정리 (같은 질문 재확인)

같은 질문이 다시 들어와서, 지금까지 진행 상황을 정리한다. `ai/develop-ticket.md`상
"[W1] Android 프로젝트 셋업" 티켓은 아직 **진행중** (완료 처리 안 함 — 검증 전이라 당연함).

**지금까지 한 일**:
1. Hilt/Room/Retrofit/KSP 의존성 설치 + Hilt 셋업 + DI 확인용 더미 클래스 작성
2. Gradle Sync 에러 2건 순서대로 수정: Hilt 버전(2.57.1→2.60.1, AGP 9 비호환), KSP 버전
   (2.2.10-2.0.2→2.3.9, AGP 9 빌트인 코틀린 비호환)

**아직 확인 안 된 것**: 두 번째 수정(KSP 2.3.9) 이후 Gradle Sync가 실제로 성공했는지,
그리고 실기기/에뮬레이터에서 앱을 실행해봤는지 — 이 대화에서 아직 결과를 못 들었음.

**검증 체크리스트 (요약, 상세 설명은 이 파일 위쪽 "지금부터 직접 해야 할 것" 참고)**:
- [ ] Gradle Sync가 에러 없이 끝났는가 (아직 에러 나면 메시지 그대로 알려주면 계속 봐줌)
- [ ] Sync 성공 후, 실기기/에뮬레이터에 Run 눌러서 앱이 크래시 없이 켜지는가
- [ ] 화면에 "Hello Hilt 연결됨!" 문구가 보이는가 → 보이면 완료 조건 둘 다 통과
      (앱 빌드/실행 + Hilt DI 그래프 동작 확인)
- [ ] 통과했으면 `HiltSmokeTestRepository.kt`와 `MainActivity`의 관련 코드는 지워도 됨

다음 메시지에 Sync 결과나 실행 결과(에러든 성공이든)를 알려주면 그에 맞춰 진행하면 된다.

---

## 요청

"[W1] Android 프로젝트 셋업" 티켓 작업 내용대로 Hilt/Room/Retrofit 의존성을 설치하고
Hilt를 셋업, 지난 답변에서 설명한 "더미 클래스로 Hilt DI 그래프 확인" 코드까지 작성해달라는
요청. 프로젝트 환경설정은 AI 도움을 받아 진행하기로 함 — CLAUDE.md의 "코드는 개발자가 직접
작성" 원칙의 예외인 "단순 보일러플레이트"에 해당한다고 보고 직접 작성함.

## 버전을 실제로 조회해서 결정함 (추측 아님)

기존 지식(학습 시점 기준)이 지금(2026-08)과 어긋날 수 있어서 웹 검색으로 실제 최신/호환
버전을 확인한 뒤 적용했다.

- **Hilt 2.57.1** — [Android 공식 문서](https://developer.android.com/training/dependency-injection/hilt-android)의 Gradle 설정 예시 기준. 2026-02-21 릴리스, **AGP 9 지원**이 이 버전부터 추가됨(이 프로젝트가 AGP 9.3.1이라 중요), **Java 17 요구**.
- **KSP 2.2.10-2.0.2** — 이 프로젝트 Kotlin 버전(2.2.10)과 **정확히 매칭되는 유일한 조합**([Maven Repository](https://mvnrepository.com/artifact/com.google.devtools.ksp/com.google.devtools.ksp.gradle.plugin/2.2.10-2.0.2)). KSP는 Kotlin 버전과 접두어가 정확히 일치해야만 동작해서 임의로 고르면 안 되는 값.
- **Room 2.8.4** — [Android 공식 문서](https://developer.android.com/jetpack/androidx/releases/room) 기준 최신 안정 버전(2025-11-19). Room 3.0은 2026-03에 알파로 막 시작된 KMP 전용 대개편 버전이라 아직 이 프로젝트에 쓰기엔 이르다고 판단해 제외.
- **Retrofit 2.11.0 / OkHttp 4.12.0** — Retrofit 3.0.0 / OkHttp 5.x가 이미 나와 있지만, 지금 이 티켓에서는 "의존성만 구성"이 목적이고 아직 실제로 API를 호출하지 않으므로 검증된 지 오래된 안정 조합(2.x)을 골랐다. 실제 API 연동 티켓(궤적 업로드 등) 시점에 3.0.0으로 올릴지 다시 판단하면 됨.

## 한 것

### 1. 의존성 등록 (`gradle/libs.versions.toml`)
버전 5개(`ksp`, `hilt`, `room`, `retrofit`, `okhttp`)와 라이브러리 7개, 플러그인 2개
(`ksp`, `hilt-android`) 추가. 기존 컨벤션대로 버전을 하드코딩하지 않고 카탈로그에만 등록.

### 2. 플러그인 적용
- 루트 `build.gradle.kts`: `ksp`, `hilt.android` 플러그인을 `apply false`로 선언 (버전만 고정, 실제 적용은 app 모듈에서)
- `app/build.gradle.kts`: 두 플러그인을 실제로 적용하고, `dependencies`에 Hilt/Room(ksp 컴파일러 포함)/Retrofit/OkHttp 추가

### 3. Java/Kotlin 버전 17로 상향 (`app/build.gradle.kts`)
Hilt 2.57.1이 Java 17을 요구해서 `compileOptions`의 source/targetCompatibility를 11→17로
올렸고, Kotlin 컴파일러도 같은 버전을 쓰도록 `kotlin { jvmToolchain(17) }`을 추가함 (Java
쪽만 17로 올리고 Kotlin 쪽을 안 맞추면 "jvm target compatibility should be the same" 에러가
남 — 이게 초심자가 Hilt 붙일 때 제일 자주 만나는 에러 중 하나).

### 4. Hilt 진입점 (`app/src/main/java/com/runpassport/app/RunPassportApplication.kt`, 신규)
```kotlin
package com.runpassport.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RunPassportApplication : Application()
```
`AndroidManifest.xml`의 `<application>` 태그에 `android:name=".RunPassportApplication"` 추가함
— 이걸 안 하면 Hilt가 앱 시작 시점에 자기 그래프를 초기화할 진입점을 못 찾아서 크래시남.

### 5. DI 스모크 테스트용 더미 클래스 (`app/src/main/java/com/runpassport/app/HiltSmokeTestRepository.kt`, 신규)
```kotlin
package com.runpassport.app

import javax.inject.Inject

// Hilt DI 그래프 동작 확인용 임시 클래스 — MainActivity 화면에서 값이 보이면 지워도 됨.
class HiltSmokeTestRepository @Inject constructor() {
    fun greet(): String = "Hilt 연결됨"
}
```

### 6. MainActivity에서 주입받아 화면에 표시 (`MainActivity.kt`, 수정)
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var hiltSmokeTestRepository: HiltSmokeTestRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RunpassportTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = hiltSmokeTestRepository.greet(),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
```
(`Greeting`/`GreetingPreview` composable은 그대로 재사용 — `name` 파라미터에 "Android" 대신
Hilt가 주입해준 값이 들어감)

## 지금부터 직접 해야 할 것 (검증)

1. Android Studio에서 **Gradle Sync** — 상단에 "Sync Now" 배너가 뜨면 눌러서 새 의존성을
   내려받는다. (이 세션엔 Android SDK/Gradle 실행 환경이 없어서 제가 직접 빌드해볼 수는
   없음 — Sync/빌드 결과를 알려주면 에러 메시지를 같이 보고 고칠 수 있음)
2. Sync가 끝나면 지난번에 설명한 방식대로 **실기기(또는 에뮬레이터)에서 Run** — 화면에
   "Hello Hilt 연결됨!" 문구가 보이면:
   - 앱이 실기기에서 빌드/실행됨 ✅
   - Hilt DI 그래프가 정상 동작함 ✅ (그래프 구성 + 실제 인스턴스 생성 + Activity까지 전달 확인)
3. 확인되면 `HiltSmokeTestRepository.kt` 파일과 `MainActivity`의 관련 코드(주입 필드,
   `Greeting`에 넘기는 값)는 지워도 된다 — 검증용 스모크 테스트였을 뿐, 이후 실제 화면/기능
   개발할 때 진짜 Hilt 사용 예시가 자연스럽게 생긴다.

## 확인이 필요합니다

- Gradle Sync 시 버전 충돌이나 "찾을 수 없는 아티팩트" 에러가 나면, 위에 적어둔 버전 중
  하나가 이 시점 기준으로 이미 더 갱신됐을 가능성이 있음 — 에러 메시지를 그대로 붙여넣어
  주면 해당 버전만 다시 확인해서 고치겠음.
- `kotlin { jvmToolchain(17) }` 블록이 "Unresolved reference: kotlin" 같은 에러를 내면
  (이 프로젝트가 `org.jetbrains.kotlin.android` 플러그인을 명시적으로 선언하지 않고
  `kotlin.plugin.compose`만 쓰고 있어서 발생 가능성 있음), 알려주면 다른 방식
  (`android { compileOptions }`만으로 처리하거나 kotlin-android 플러그인을 명시적으로
  추가하는 방식)으로 바꾸겠음.
