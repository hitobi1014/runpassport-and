---
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git add:*), Bash(git commit:*), Read
argument-hint: (없음 — staged 변경사항 기준, 없으면 워킹트리 변경사항)
description: 변경사항을 분석해 Conventional Commits 형식 메시지를 만들고, 확인 후 커밋까지 실행
---

# 커밋

## 목적

현재 변경사항을 분석해서 Conventional Commits 형식의 한국어 커밋 메시지를 만들고,
사용자 확인 후 실제로 커밋까지 실행한다.

## 절차

1. `git status --short`로 변경된 파일 목록을 확인한다.
2. `git diff --staged`로 staged 변경사항을 확인한다. staged가 없으면 `git diff HEAD`로
   워킹트리 변경사항을 대신 분석하고, 이 경우 어떤 파일을 add할지 채팅에 먼저 알려준다.
3. 변경사항이 서로 다른 관심사(예: 기능 코드 + 무관한 리팩토링, 두 개의 독립적인 기능)에
   걸쳐 있으면 하나로 묶지 말고 분리 커밋을 제안한다.
4. 아래 형식으로 커밋 메시지(들)를 만든다.

형식:

```
type: 제목 (50자 이내)

- 변경 내용 요약 1
- 변경 내용 요약 2
```

type 규칙:

- `feat`: 새 기능
- `fix`: 버그 수정
- `refactor`: 리팩토링 (동작 변화 없음)
- `chore`: 설정, 패키지, 빌드 스크립트 등
- `docs`: 문서 (`CLAUDE.md`, `note/`, `.claude/commands/` 등)
- `test`: 테스트 코드 추가/수정

규칙:

- 제목은 한글로 작성하고, type prefix만 영어로 쓴다.
- 변경 내용 요약은 실제로 바뀐 만큼만 작성한다 (사소한 변경이면 본문 생략 가능).
- 지침·문서 파일(`CLAUDE.md`, `.claude/commands/`, `note/`, `ai/`)만 바뀌었으면 `docs:`로
  분류하고, 코드 변경과 섞여 있으면 코드 쪽 type을 우선한다.

5. 만든 커밋 메시지를 채팅에 보여주고 사용자 확인을 받는다.
6. 확인되면 관련 파일만 `git add <파일...>`로 스테이징한다 (`git add -A`/`git add .`은
   의도치 않은 파일까지 포함될 수 있어 쓰지 않는다).
7. 커밋 메시지는 줄바꿈이 깨지지 않도록 heredoc으로 전달한다.

```
git commit -m "$(cat <<'EOF'
type: 제목

- 변경 내용 요약
EOF
)"
```

8. pre-commit 훅이 실패하면 원인을 고치고 다시 커밋한다. **`--no-verify`는 쓰지 않는다.**
9. 분리 커밋을 제안했다면 5~8을 커밋별로 반복한다.

## 하지 않는 것

- `git push`는 하지 않는다 (별도로 요청받은 경우에만).
- `--amend`는 사용자가 명시적으로 요청한 경우에만 쓴다.
