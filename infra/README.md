# Infrastructure

배포·운영 설정과 스크립트를 관리합니다. Gradle 모듈이 아니며 Java 코드는 두지 않습니다.

| 경로 | 역할 |
| --- | --- |
| `docker` | 애플리케이션 Dockerfile과 로컬·배포용 Compose 설정 |
| `monitoring` | Prometheus·Grafana·Loki 설정 |
| `gcp` | GCP 인프라·배포 설정 |

현재 디렉터리 구조만 준비되어 있으며 실제 배포 설정은 도입 시 추가합니다. 실제 비밀값은 저장하지 않고 실행환경에서 주입합니다. GitHub Actions 워크플로는 `.github/workflows`에서 관리합니다.
