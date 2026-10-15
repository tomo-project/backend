# Workflow

이 문서는 Fork 기반으로 Issue 생성부터 Pull Request, Merge까지의 기본 협업 흐름을 정의합니다.

## 기본 흐름

```text
원본 Repository Fork
   ↓
Fork Repository Clone
   ↓
upstream 등록
   ↓
Issue 생성
   ↓
main 최신화
   ↓
작업 Branch 생성
   ↓
개발 및 Commit
   ↓
Fork Repository에 Push
   ↓
원본 Repository로 Pull Request 생성
   ↓
Review / Test
   ↓
Merge
   ↓
Fork Repository main 최신화
```

## 1. Repository Fork 및 Clone

개발자는 원본 Repository에서 직접 작업하지 않고 자신의 GitHub 계정으로 Repository를 Fork하여 작업합니다.

원본 Repository:

```text
today-it/backend
```

원본 Repository를 자신의 계정으로 Fork한 후 Fork Repository를 Clone합니다.

```bash
git clone https://github.com/{GitHub-Username}/backend.git
cd backend
```

Clone한 Repository의 `origin`은 자신의 Fork Repository를 가리킵니다.

```text
origin → {GitHub-Username}/backend
```

## 2. upstream 등록

원본 Repository의 변경 사항을 받아오기 위해 원본 Repository를 `upstream`으로 등록합니다.

먼저 현재 Remote를 확인합니다.

```bash
git remote -v
```

`upstream`이 없다면 한 번만 추가합니다.

```bash
git remote add upstream https://github.com/today-it/backend.git
```

다시 확인합니다.

```bash
git remote -v
```

정상적으로 등록되었다면 다음과 같은 구조가 됩니다.

```text
origin    → 개인 Fork Repository
upstream  → today-it/backend
```

`upstream` 등록은 최초 한 번만 하면 됩니다.

## 3. Issue 생성

모든 작업은 가능한 한 Issue를 기준으로 시작합니다.

작업 목적에 맞는 Issue Form을 선택하고 다음 내용을 작성합니다.

* 작업 목적 또는 문제 상황
* 필요한 변경 사항
* 세부 작업 목록
* 완료 조건

## 4. main 최신화

새로운 작업을 시작하기 전에 원본 Repository의 최신 `main`을 자신의 Fork Repository에 반영합니다.

```bash
git checkout main
git fetch upstream
git merge --ff-only upstream/main
git push origin main
```

각 Remote의 역할은 다음과 같습니다.

```text
upstream/main
        ↓
local main
        ↓
origin/main
```

즉, 원본 Repository의 `main`을 기준으로 로컬과 자신의 Fork Repository를 동일한 상태로 맞춥니다.

> `git merge --ff-only`은 로컬 `main`과 `upstream/main`이 분기된 경우 중단됩니다. 이때 강제로 초기화하지 말고 `git status`와 Commit 이력을 먼저 확인합니다.

## 5. 작업 Branch 생성

최신화된 `main`을 기준으로 작업 브랜치를 생성합니다.

```bash
git checkout main
git checkout -b feature/12-profile-update
```

브랜치 이름은 [Convention](CONVENTION.md)의 Branch Convention을 따릅니다.

작업은 반드시 별도의 작업 브랜치에서 진행하며 `main`에서 직접 개발하지 않습니다.

## 6. 개발 및 Commit

Issue의 작업 범위를 기준으로 기능을 구현하고 의미 있는 작업 단위로 Commit합니다.

```text
✨ Feat: 프로필 수정 API 구현
🧪 Test: 프로필 수정 서비스 테스트 추가
```

Commit Message는 [Convention](CONVENTION.md)의 Commit Convention을 따릅니다.

## 7. Fork Repository에 Push

작업이 완료되면 작업 브랜치를 자신의 Fork Repository에 Push합니다.

```bash
git push -u origin feature/12-profile-update
```

작업 브랜치는 다음과 같이 Push됩니다.

```text
Local
feature/12-profile-update
        ↓
origin
        ↓
{GitHub-Username}/backend
feature/12-profile-update
```

원본 Repository인 `today-it/backend`에는 작업 브랜치를 직접 Push하지 않습니다.

## 8. Pull Request 생성

Fork Repository에 작업 브랜치를 Push한 후 원본 Repository의 `main`을 대상으로 Pull Request를 생성합니다.

PR 방향은 다음과 같습니다.

```text
{GitHub-Username}/backend
feature/12-profile-update
        ↓
Pull Request
        ↓
today-it/backend
main
```

Pull Request 생성 시 다음 내용을 확인합니다.

* Base Repository: `today-it/backend`
* Base Branch: `main`
* Head Repository: 자신의 Fork Repository
* Compare Branch: 자신의 작업 브랜치
* PR Template 작성
* 관련 Issue 연결
* 주요 변경 사항 작성
* 테스트 결과 확인

## 9. Review / Test

Merge 전에 다음 사항을 확인합니다.

* 요구사항이 충족되었는지 확인합니다.
* 기존 기능에 예상하지 못한 영향이 없는지 확인합니다.
* 필요한 테스트가 통과했는지 확인합니다.
* 리뷰 의견을 확인하고 필요한 내용을 반영합니다.

리뷰 내용을 반영해야 하는 경우 기존 작업 브랜치에서 수정한 후 다시 Push합니다.

```bash
git add .
git commit -m "🐛 Bug: 리뷰 내용 반영"
git push origin feature/12-profile-update
```

동일한 Branch에 Push하면 기존 Pull Request에 변경 사항이 자동으로 반영됩니다.

## 10. Merge 후 main 최신화

Pull Request가 Merge되면 원본 Repository의 `main`에 새로운 변경 사항이 생깁니다.

다음 작업을 시작하기 전에 자신의 로컬 `main`과 Fork Repository의 `main`을 원본 Repository와 다시 동일하게 맞춥니다.

```bash
git checkout main
git fetch upstream
git merge --ff-only upstream/main
git push origin main
```

이 과정을 기억하면 됩니다.

```text
upstream이 있으면

fetch
  ↓
merge --ff-only
  ↓
push
```

즉, PR이 Merge된 이후에는 다음 명령어를 기준으로 `main`을 최신 상태로 유지합니다.

```bash
git checkout main
git fetch upstream
git merge --ff-only upstream/main
git push origin main
```

## Fork 후 Clone만 한 경우

Fork와 Clone까지만 진행했고 아직 `upstream`을 설정하지 않았다면 먼저 확인합니다.

```bash
git remote -v
```

`upstream`이 보이면 추가 설정 없이 사용하면 됩니다.

`upstream`이 없다면 한 번만 등록합니다.

```bash
git remote add upstream https://github.com/today-it/backend.git
```

이후 PR Merge 후에는 동일하게 다음 명령어를 사용합니다.

```bash
git checkout main
git fetch upstream
git merge --ff-only upstream/main
git push origin main
```

## 핵심 명령어

Fork Repository를 사용하면서 원본 Repository의 `main`과 동기화할 때는 다음 네 줄을 기본으로 사용합니다.

```bash
git checkout main
git fetch upstream
git merge --ff-only upstream/main
git push origin main
```

```text
upstream 확인
   ↓
fetch
   ↓
merge --ff-only
   ↓
push
```
