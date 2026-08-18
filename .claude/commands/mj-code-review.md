---
allowed-tools: Read, Grep, Glob, Bash(git diff:*), Bash(git status:*), Bash(mkdir:*), Write
argument-hint: [ base-ref ] (선택, 예: main — 생략하면 staged/워킹트리 변경사항 기준)
description: 현재 변경사항을 CLAUDE.md 코드 리뷰 기준으로 리뷰하고 ai/code-review/에 기록
---

# 코드 리뷰 (mj-code-review)

## 목적

현재 변경사항을 `CLAUDE.md`의 "코드 리뷰 기준"에 따라 리뷰한다. 특정 티켓의 완료 조건
대조가 목적이면 이 커맨드 대신 `/ticket-review`를 쓴다.

**코드는 직접 수정하지 않는다 — 리뷰만 작성한다.**

## 리뷰 제외 대상

- `.md` 파일 (문서, 지침, 결과 등)

## 리뷰 대상 결정

- 인자(`$ARGUMENTS`)가 있으면 그 base-ref와 비교한다 (`git diff $ARGUMENTS`).
- 인자가 없으면 `git diff --staged`로 staged 변경사항을 확인하고, 없으면 `git diff HEAD`로
  워킹트리 변경사항을 확인한다.

## 리뷰 기준 (CLAUDE.md 기반)

1. **아키텍처 위반** — ViewModel 밖에서의 상태 관리, 상태 호이스팅 안 된 Composable,
   단방향 데이터 흐름(UDF) 위반
2. **Null 안정성** — `!!` 사용, `?:`/`requireNotNull`/`checkNotNull`로 대체 가능한지
3. **코루틴 누수** — `viewModelScope`/`LaunchedEffect`/`rememberCoroutineScope` 밖에서
   직접 코루틴을 실행하는지, 예외 처리 누락
4. **하드코딩** — 문자열/색상/치수/매직 넘버가 `strings.xml`/테마를 거치지 않는지
5. **네이밍** — 패키지/클래스/함수/상수 컨벤션 준수 여부
6. **위치/권한 처리 누락** — 위치 API 호출 전 권한 체크, Foreground Service의 모든
   종료 경로 처리
7. **불필요한 코드** — 주석 처리된 코드, 사용하지 않는 import
8. **가독성/유지보수성** — 함수/변수명 명확성, 단일 책임 원칙

## 리뷰 출력 형식

각 항목을 아래 형식으로 작성한다:

```
### [파일명 또는 변경 범위]
- **[심각도: 🔴 오류 / 🟡 경고 / 🟢 제안]** 내용 (`파일:라인`)
  - 문제점: ...
  - 개선 방향: ...
```

마지막에 전체 요약(잘된 점 / 개선 필요 점)을 작성한다.

## 결과 저장

- `ai/code-review/` 디렉토리가 없으면 만든다.
- 파일명: `ai/code-review/{날짜:YYYY-MM-DD}-{브랜치명}.md`
- 같은 날 같은 브랜치로 다시 실행하면 파일명 끝에 `_v2`처럼 버전을 붙여 이전 리뷰를
  덮어쓰지 않는다.

## 출력

파일 저장 후 채팅에는 저장 경로 + 🔴 개수 + 핵심 이슈 1~2줄만 요약해서 보여준다. 리뷰
전문을 채팅에 다시 출력하지 않는다 — 파일이 원본이다.
