# Agent Rules

이 디렉터리는 Agent 전용 행동 지침만 관리합니다. 사람과 Agent가 함께 사용하는 코드 규칙의 단일 원본은 [docs/code-rules/README.md](../../docs/code-rules/README.md)입니다.

## 적용 범위

- 이 저장소에는 Cycle, Task 원장과 제품 설계 Workflow를 사용하지 않습니다.
- Git 협업은 [Convention](../../docs/CONVENTION.md)과 [Workflow](../../docs/WORKFLOW.md)를 따릅니다.
- 하위 경로에 `AGENTS.md`가 있으면 해당 경로에서는 더 가까운 규칙을 우선합니다.

## 필수 읽기 순서

1. [코드 규칙](../../docs/code-rules/README.md)
2. [명세 원본 규칙](../../docs/code-rules/specification.md)
3. 수정할 코드와 테스트

## Agent 행동 규칙

- 코드 변경 전에 Git 변경과 현재 브랜치를 확인합니다.
- 기술 스택과 기존 코드 구조를 확인하고 관련 코드·테스트를 읽은 뒤 최소 범위로 변경합니다.
- 결정되지 않은 프레임워크·라이브러리·코드 스타일을 임의로 도입하지 않습니다.
- 구현 대상의 요구사항 ID, 기능 ID, API ID와 결정상태를 확인합니다.
- 명세의 구현 가능 여부는 [명세 원본 규칙](../../docs/code-rules/specification.md)의 결정상태 기준을 따릅니다. 원본에 없는 승인 상태를 임의로 요구하지 않습니다.
- 명세와 코드가 충돌하면 임의로 맞추지 않고 충돌 내용을 먼저 알립니다.
- 요청 범위 밖의 리팩터링과 의존성 추가를 피합니다.
- 기존 사용자 변경을 삭제하거나 덮어쓰지 않습니다.
- 자동 포맷터와 정적 분석 설정이 있으면 수동 선호보다 해당 설정을 우선합니다.
- 같은 규칙을 Agent 문서에 복사하지 않고 `docs/code-rules/`를 참조합니다.

## 충돌 우선순위

1. 사용자의 현재 요청과 승인된 기술 결정
2. 수정 대상에 더 가까운 `AGENTS.md`
3. `docs/code-rules/`의 공통 코드 규칙
4. 기존 코드와 프로젝트 설정에서 일관되게 사용되는 규칙
5. `docs/`의 Git 협업 문서
