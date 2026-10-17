# TOMO Backend

TOMO의 HTTP API 서버입니다. 기능별 업무 코드는 `apps:api`에 두고, 외부 시스템 연동 모듈은 필요할 때 추가합니다.

## 개발 환경

- Java 21 (JDK)
- Spring Boot 3.5.16, Spring MVC
- Gradle 8.14.3 (Wrapper 포함)
- 기본 패키지: `com.tomo`

`JAVA_HOME`을 JDK 21 경로로 설정합니다. IntelliJ에서는 루트 `settings.gradle`을 열고 Gradle JVM을 Java 21로 지정합니다.

## 프로젝트 구조

```text
backend/
├── apps/api/         # 실행 클래스, 기본 설정, 서버 시작 테스트
├── integrations/     # storage·redis·messaging 연동 라이브러리
├── infra/            # docker·monitoring·gcp 배포 설정
├── gradle/wrapper/   # Gradle Wrapper
└── docs/             # 코드·명세·협업 규칙
```

`integrations:storage`, `integrations:redis`, `integrations:messaging`은 Java 라이브러리 모듈이며 `apps:api`에서 사용합니다. 각 모듈에는 GCS SDK, Spring Data Redis·Redisson, Spring AMQP 의존성이 추가되어 있습니다. 실제 클라이언트 구성·연동 구현은 후속 작업입니다. `infra`는 Gradle 모듈이 아닌 배포 설정 디렉터리입니다. 배치 앱은 아직 만들지 않습니다.

Spring 라이브러리 버전은 루트 `build.gradle`에서 Spring Boot BOM을 공통 적용해 관리합니다. 각 모듈에 같은 BOM 선언을 반복하지 않으며 품질 검사 도구의 의존성에는 적용하지 않습니다. JWT는 JJWT 0.13.0, API 문서는 springdoc Scalar 2.9.1, Redis 분산 락은 Redisson 3.52.0, GCS는 Google Cloud Storage 2.75.0을 사용합니다. JUnit·Mockito·AssertJ·Testcontainers는 Boot BOM을 기준으로 관리합니다.

## 서버 실행과 패키징

### Windows (PowerShell)

```powershell
# 서버 실행
.\gradlew.bat :apps:api:bootRun

# 실행 JAR 생성
.\gradlew.bat :apps:api:bootJar
```

### macOS·Linux

```bash
# 서버 실행
sh gradlew :apps:api:bootRun

# 실행 JAR 생성
sh gradlew :apps:api:bootJar
```

서버 기본 포트는 `8080`이며 `SERVER_PORT` 환경변수로 변경합니다. 실행 JAR은 `apps/api/build/libs/`에 생성됩니다.

### 실행 프로필과 연동 상태

- 일반 서버는 `apps/api/src/main/resources/application.yaml`의 DataSource로 MySQL에 연결합니다. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 실행환경에 맞게 지정합니다. 환경변수의 기본값은 없습니다. MySQL 서버와 DB·계정은 실행 전에 준비해야 합니다.
- 테스트는 `apps/api/src/test/resources/application-test.yaml`과 `test` 프로필을 사용합니다. `jdbc:tc:mysql:8.4.6:///tomo_test`에 의해 Testcontainers가 임시 MySQL을 자동 생성하고 정리합니다. 테스트 DB를 Compose로 미리 띄우거나 고정 포트를 지정할 필요가 없습니다.
- DB 통합 테스트와 커밋 전 `check`를 실행할 때는 Docker 엔진이 실행 중이어야 합니다. 첫 실행에는 MySQL 이미지 다운로드가 필요할 수 있습니다. 사용자별 테스트 설정은 `test` 프로필의 YAML이나 Spring 환경변수로 조정하고 실제 비밀값은 저장하지 않습니다. Redis·RabbitMQ·GCS 연결 테스트는 아직 없습니다.
- Spring Security 기본 보호를 유지합니다. 제품의 JWT·OAuth2 인증과 접근 정책은 아직 구현하지 않았으므로 API 문서를 포함한 보호 경로는 인증이 필요합니다.
- OpenAPI·Scalar는 `OPENAPI_ENABLED`에 `true` 또는 `false`를 명시해 함께 켜거나 끕니다. 경로는 각각 `/v3/api-docs`, `/scalar`이며 활성화해도 Spring Security의 접근 보호는 유지됩니다. 테스트에서는 항상 비활성화합니다.
- 관리 엔드포인트는 `MANAGEMENT_ENDPOINTS=health`처럼 노출 대상을 명시하며 상세 정보는 숨깁니다. Prometheus 지표 공개가 필요하면 `MANAGEMENT_ENDPOINTS=health,prometheus`로 지정합니다. 접근 권한과 실제 수집 서버·대시보드는 별도로 구성합니다.
- WebSocket·STOMP, Scheduler와 연동 모듈의 실제 처리 흐름은 기능 구현 시 추가합니다. Prometheus·Grafana·Loki 서버, Docker·GCP 배포와 CI는 아직 구성하지 않았습니다.

### Redis·RabbitMQ 접속 설정

| 환경변수 | 로컬 설정 예시 (기본값 없음) |
| --- | --- |
| `REDIS_HOST`, `REDIS_PORT` | `localhost`, `6379` |
| `REDIS_USERNAME`, `REDIS_PASSWORD` | 빈 값 |
| `REDIS_DATABASE` | `0` |
| `RABBITMQ_HOST`, `RABBITMQ_PORT` | `localhost`, `5672` |
| `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | `guest`, `guest` (로컬 개발용) |
| `RABBITMQ_VIRTUAL_HOST` | `/` |

Redis 접속·명령 타임아웃은 각각 3초, RabbitMQ 접속 타임아웃은 5초입니다. 위 Redis 설정은 Spring Data Redis 연결에 적용되며 Redisson 클라이언트 생성은 아직 구현 전입니다. 캐시 TTL, 분산 락 정책, RabbitMQ 큐·라우팅·재시도 정책은 관련 기능 구현 시 정합니다.

Flyway는 `classpath:db/migration`을 사용하고 실행 전 마이그레이션 이력을 검증하며 `clean`을 비활성화합니다. 현재 마이그레이션 파일은 없고, JPA의 자동 스키마 변경도 `ddl-auto: none`으로 막아둔 상태입니다.

## 커밋 전 자동 검사

클론한 저장소마다 JDK 21 환경에서 한 번 실행해 Git 훅을 활성화합니다.

### Windows (PowerShell)

```powershell
.\gradlew.bat installGitHooks
```

### macOS·Linux

```bash
sh gradlew installGitHooks
```

설치 후 `git commit` 전에 `.githooks/pre-commit`이 전체 Java 모듈의 `check`를 자동 실행합니다. 테스트·ArchUnit·Spotless·SpotBugs·Checkstyle 중 하나라도 실패하면 커밋을 중단합니다. Checkstyle은 테스트의 Javadoc을 검사하지 않으며 JaCoCo 계측과 보고서 생성은 기본 비활성화 상태입니다.

설치는 해당 클론의 `core.hooksPath`를 `.githooks`로 설정하고, 설치 시 사용한 Gradle JVM 경로를 `tomo.hookJavaHome`에 저장합니다. IDE에서도 같은 JVM으로 검사하며 JDK 경로가 바뀌면 설치 명령을 다시 실행합니다.

검사는 스테이징된 내용만이 아닌 현재 작업 트리 전체를 대상으로 합니다. 부분 스테이징한 파일은 커밋할 내용과 작업 트리의 차이도 확인합니다. 로컬 훅은 `--no-verify`로 건너뛸 수 있으며 CI 검증은 아직 연결하지 않았습니다.

## 수동 검사 및 포맷 수정

커밋 전에 미리 검사하거나 오류를 수정할 때만 아래 명령을 직접 실행합니다. 자동 훅과 수동 `check`는 검사만 하며 코드를 수정하지 않습니다.

### Windows (PowerShell)

```powershell
# 전체 검사
.\gradlew.bat check

# Java 포맷 자동 수정
.\gradlew.bat spotlessApply

# 아키텍처 테스트만 실행
.\gradlew.bat :apps:api:test --tests 'com.tomo.architecture.ArchitectureTest'
```

### macOS·Linux

```bash
# 전체 검사
sh gradlew check

# Java 포맷 자동 수정
sh gradlew spotlessApply

# 아키텍처 테스트만 실행
sh gradlew :apps:api:test --tests 'com.tomo.architecture.ArchitectureTest'
```

포맷 오류는 `spotlessApply`로 수정한 뒤 변경 파일을 다시 스테이징하고 커밋합니다. 다른 검사 실패는 오류 메시지와 아래 보고서를 확인해 수정합니다.

### 검사 결과 확인

각 모듈의 `build/reports/`에서 확인합니다. 소스나 테스트가 없으면 해당 검사와 보고서 생성을 건너뛸 수 있습니다.

| 도구 | 보고서 경로 (모듈 기준) |
| --- | --- |
| 테스트·ArchUnit | `build/reports/tests/test/index.html` |
| Checkstyle | `build/reports/checkstyle/main.html`, `test.html` |
| SpotBugs | `build/reports/spotbugs/main.html`, `test.html` |

JaCoCo는 추후 CI에서 `gradlew -Pcoverage=true check jacocoTestReport`로 별도 실행할 수 있도록 준비되어 있습니다. 활성화하면 `build/reports/jacoco/test/html/index.html`에 보고서를 생성하며 최소 커버리지 기준은 아직 없습니다.

## 제공 항목

- GitHub Issue Forms
- Pull Request Template
- GitHub Label 초기화 스크립트
- Commit & Branch Convention
- GitHub Workflow

## 문서

프로젝트에서 사용하는 협업 정책은 `docs/`에서 확인할 수 있습니다.

- [Convention](docs/CONVENTION.md)
- [Workflow](docs/WORKFLOW.md)
- [Labels](docs/LABELS.md)
- [코드 규칙](docs/code-rules/README.md)
- [명세 원본 규칙](docs/code-rules/specification.md)

템플릿으로 새 저장소를 생성한 뒤 Label은 별도로 초기화해야 합니다.

```bash
./scripts/setup-labels.sh
```

Windows PowerShell에서는 다음 스크립트를 사용할 수 있습니다.

```powershell
.\scripts\setup-labels.ps1
```
