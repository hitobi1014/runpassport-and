# 요청 처리 현황

`.claude/commands/request.md`에 정의된 0~4단계 절차를 따라 진행했다. 전체 완료.

## 0단계 — 사전 조사 (완료)

- 기존 스택 확인: 순수 Compose 템플릿(Hilt/Coroutines-Flow/Room/Retrofit 미도입)
- 기존 `.claude/commands/` 6개 파일에서 다른 프로젝트(`ticket-system-fe`) 참조 오염,
  백엔드(JPA) 컨벤션 오기재 발견

## 1단계 — CLAUDE.md 작성 (완료)

프로젝트 개요, 안드로이드 개발 표준 규칙(이유 포함), 티켓 관리 규칙, 주니어 체크리스트,
코드 리뷰 기준, 커맨드 6종 안내, 참고 문서 위치를 한국어로 작성. 기존 프로젝트
정보/Gradle 커맨드/구조/Testing 섹션은 유지.

## 2단계 — 커맨드 6종 작성 (완료)

| 파일 | 처리 내용 |
|---|---|
| `commit.md` | 깨진 `ticket-system-fe` 링크 제거, 확인 후 실제 커밋까지 실행하는 절차로 재작성 |
| `create-pr.md` | pbcopy → `ai/git-pr.md` 폴백 유지하며 정리 |
| `mj-code-review.md` | React/Zustand 기준(다른 프로젝트 것) → Android(MVVM/코루틴/위치권한) 기준으로 전면 재작성 |
| `request.md` | frontmatter 위치 오류(파일 최상단에 있지 않아 스킬 파싱 깨짐) 수정 + 범용 절차 추가 |
| `sync-ticket.md` | 노션 마일스톤/Develop DB 동기화 절차 신규 작성 (스키마 우선 확인, 자동 삭제 금지) |
| `ticket-review.md` | 백엔드(JPA/Fetcher) 컨벤션 언급 → Android 컨벤션 예시로 교체 |

## 3단계 — sync-ticket.md 상세 작성 (완료, 2단계에 포함)

## 4단계 — 기능명세.md / develop-ticket.md 작성 (완료)

Notion MCP로 다음을 읽었다:
- '서비스 기획' 페이지 (2026-08-02) — 서비스 컨셉, 기능 구성, 활용 API
- '기능 명세' 페이지 (2026-08-06) — 체크인 → 궤적 대조 피벗, 완주 판정 로직, 기술스택,
  데이터 모델, 일정
- 'Run패스포트 Develop' DB (담당 영역=프론트엔드, 20건) + 'Run패스포트 마일스톤' DB (W1~W7)
  — 스키마를 먼저 조회해 속성명/옵션값(`영역`="프론트엔드" 등)을 실제 값으로 확인 후 사용

**`note/기능명세.md`**: 서비스 개요, 핵심 기능 목록 표, 화면별 상세 명세(코스 목록/상세,
러닝 트래킹, 완주 결과, 스탬프 컬렉션, 쿠폰함), 클라이언트 기술 요구사항, MVP/확장 범위
구분으로 작성. 노션 원문의 웹앱 체크인 방식은 이미 문서 내에서 GPS 궤적 대조로 피벗된
것으로 명시돼 있어 그 최신 방향만 반영했다.

**`ai/develop-ticket.md`**: 노션 Develop DB의 프론트엔드 티켓 20건(각 티켓 페이지 본문의
작업 내용/완료 조건/선행 티켓까지 모두 확인)을 W1~W7 마일스톤 순서로 반영. 각 티켓은
노션 "작업 내용" 한 줄을 `기능명세.md`의 구체적 수치(예: accuracy>25m, 속도>8m/s 등)로
2~3개 세부 개발 항목으로 전개했다. "지금 진행 중"은 ID6(Android 프로젝트 셋업, In
Progress)으로 갱신.

## 확인이 필요합니다 (한 곳에 모음)

- `note/기능명세.md` 하단 "확인이 필요한 부분": Activity Recognition API 기반 부정
  방지(탈것 탑승 탐지)에 대응하는 프론트엔드 티켓이 노션에 없음 — 이번 범위에 넣을지
  판단 필요.
- 완주 판정 엔드포인트/응답 스펙은 백엔드 저장소 문서 확인이 필요함 (이 저장소 범위 밖).
- CLAUDE.md는 Hilt/Coroutines-Flow/Room 등을 "아직 코드엔 없지만 도입 시 따를 방향"으로
  미리 적어뒀음 — 실제 도입 시점에 방향이 여전히 맞는지 한 번 더 확인 권장.

## 생성/수정된 파일 목록

- `CLAUDE.md` (재작성)
- `.claude/commands/commit.md` (재작성)
- `.claude/commands/create-pr.md` (재작성)
- `.claude/commands/mj-code-review.md` (재작성)
- `.claude/commands/request.md` (재작성)
- `.claude/commands/sync-ticket.md` (신규)
- `.claude/commands/ticket-review.md` (부분 수정)
- `note/기능명세.md` (신규 작성)
- `ai/develop-ticket.md` (신규 작성)
- `ai/response.md` (이 파일)

## 참조 무결성 재확인

CLAUDE.md가 언급하는 커맨드 6개(`/commit` `/create-pr` `/mj-code-review` `/request`
`/sync-ticket` `/ticket-review`) 모두 `.claude/commands/`에 실제로 존재함. CLAUDE.md가
언급하는 문서 경로(`note/기능명세.md`, `ai/develop-ticket.md`, `ai/current-ticket.md`,
`ai/code-review/`, `ai/response.md`) 중 `ai/code-review/`는 아직 리뷰를 한 번도 실행하지
않아 디렉토리가 없는 상태 — `/mj-code-review` 또는 `/ticket-review` 최초 실행 시 자동
생성됨(정상 동작).
