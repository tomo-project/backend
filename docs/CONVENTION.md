# Convention

프로젝트의 변경 이력을 일관된 형태로 관리하기 위한 Commit, Branch, Pull Request 규칙입니다.

이 문서는 협업 Convention의 단일 원본입니다. 현재 기본 브랜치는 `main`이며 별도 승인 없이 `develop`을 전제로 하지 않습니다.

## Commit Convention

### Commit Message Format

커밋 메시지는 다음 형식으로 작성합니다.

```text
<Emoji> <Type>: <Subject>

<Body> (Optional)

<Footer> (Optional)
```

### Type

변경 목적에 따라 아래 타입 중 하나를 사용합니다.

| Type | Emoji | Emoji Code | 사용 범위 |
| --- | --- | --- | --- |
| `Feat` | ✨ | `:sparkles:` | 새로운 기능 추가 또는 기존 기능 개선 |
| `Bug` | 🐛 | `:bug:` | 버그, 오류, 데이터 불일치 수정 |
| `Docs` | 📚 | `:books:` | README, 가이드 등 문서 변경 |
| `Style` | 🎨 | `:art:` | 동작에 영향을 주지 않는 코드 스타일 변경 |
| `Refactor` | 🛠 | `:hammer_and_wrench:` | 기능 변경 없는 코드·구조 개선 |
| `Test` | 🧪 | `:test_tube:` | 테스트 추가 및 수정 |
| `Chore` | ⚙️ | `:gear:` | 의존성, 설정, 개발 환경 관련 작업 |
| `DevOps` | 🐳 | `:whale:` | 배포, 인프라, CI/CD 자동화 |
| `Security` | 🔒 | `:lock:` | 인증, 인가 및 보안 관련 작업 |

### Subject

- 50자 이내로 간결하게 작성합니다.
- 무엇을 변경했는지 명확하게 드러냅니다.
- 제목 끝에 마침표를 작성하지 않습니다.
- `구현`, `추가`, `수정`, `분리`, `개선`처럼 작업 내용을 나타내는 표현을 사용합니다.

```text
✨ Feat: 사용자 프로필 수정 기능 구현
🐛 Bug: 중복 요청이 처리되는 오류 수정
🛠 Refactor: 인증 검증 로직 분리
🎨 Style: 코드 포맷팅 규칙 적용
🔒 Security: 접근 권한 검증 강화
```

### Body

제목만으로 변경 내용을 설명하기 어려울 때 작성합니다.

- 무엇을 어떻게 변경했는지 작성합니다.
- 변경한 이유나 설계 의도가 있다면 함께 기록합니다.
- 주요 비즈니스 규칙이나 영향 범위를 필요한 만큼 작성합니다.

```text
✨ Feat: 알림 설정 기능 구현

- 사용자별 알림 수신 여부를 저장하도록 구조 추가
- 설정 변경 시 입력값을 검증하도록 처리
- 관련 테스트 케이스 추가
```

### Footer

관련 이슈를 연결하거나 자동으로 종료할 때 사용합니다.

| Keyword | 사용 기준 | 예시 |
| --- | --- | --- |
| `Closes` | 일반 작업을 완료한 경우 | `Closes #10` |
| `Fixes` | 버그를 수정한 경우 | `Fixes #25` |
| `Resolves` | 논의·설계 이슈를 해결한 경우 | `Resolves #40` |

이슈를 종료하지 않고 참조만 할 경우 이슈 번호만 작성합니다.

```text
Related: #75
```

## Branch Convention

### Naming Format

```text
{type}/{issue-number}-{summary}
```

- 브랜치 이름에는 `#` 문자를 포함하지 않습니다.
- `summary`는 영문 소문자와 하이픈을 사용해 간결하게 작성합니다.
- 작업 브랜치는 최신 기본 브랜치에서 생성합니다.

| Type | 예시 | 용도 |
| --- | --- | --- |
| `feature` | `feature/12-profile-update` | 새로운 기능 개발 |
| `bug` | `bug/25-duplicate-request` | 버그 수정 |
| `refactor` | `refactor/30-auth-validation` | 코드·구조 개선 |
| `style` | `style/31-code-formatting` | 코드 스타일 및 포맷팅 수정 |
| `docs` | `docs/7-readme-update` | 문서 추가 및 수정 |
| `test` | `test/18-service-test` | 테스트 추가 및 수정 |
| `chore` | `chore/3-project-config` | 설정, 의존성, 개발 환경 작업 |
| `devops` | `devops/40-ci-pipeline` | 배포, 인프라, CI/CD 작업 |
| `security` | `security/45-access-control` | 인증·인가 및 보안 작업 |

## Pull Request Convention

### PR Title

PR 제목은 대표 커밋과 동일한 형식을 사용합니다.

```text
✨ Feat: 사용자 프로필 수정 기능 구현
```

### PR Body

저장소의 `.github/pull_request_template.md`를 사용합니다.

PR에는 최소한 다음 내용을 포함합니다.

- 작업 내용
- 관련 이슈
- 테스트 및 자체 검토 여부
- 리뷰어가 알아야 할 추가 사항

관련 이슈를 자동 종료하려면 다음 키워드를 사용합니다.

```text
Closes #10
Fixes #25
Resolves #40
```

진행 중인 이슈를 단순 참조할 때는 자동 종료 키워드 없이 이슈 번호를 연결합니다.
