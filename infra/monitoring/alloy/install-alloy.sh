#!/usr/bin/env bash
set -Eeuo pipefail

# TOMO Grafana Alloy & Journald 설치 및 구성 스크립트
# 지원 OS: Ubuntu/Debian 계열 (apt) 및 Rocky Linux/RHEL/CentOS 계열 (dnf/yum)

[[ $EUID -eq 0 ]] || { echo "root 권한으로 실행해야 합니다 (예: sudo bash $0)" >&2; exit 1; }

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_ALLOY_SRC="${SCRIPT_DIR}/config.alloy"
JOURNALD_CONF_SRC="${SCRIPT_DIR}/../journald/journald-tomo.conf"

echo "=== 1. Grafana 공식 저장소 등록 및 Alloy 설치 ==="
if command -v dnf >/dev/null 2>&1 || command -v yum >/dev/null 2>&1; then
  # RedHat / Rocky Linux / CentOS 계열
  cat <<'EOF' > /etc/yum.repos.d/grafana.repo
[grafana]
name=grafana
baseurl=https://rpm.grafana.com
repo_gpgcheck=1
enabled=1
gpgcheck=1
gpgkey=https://rpm.grafana.com/gpg.key
sslverify=1
sslcacert=/etc/pki/tls/certs/ca-bundle.crt
EOF
  if command -v dnf >/dev/null 2>&1; then
    dnf install -y alloy
  else
    yum install -y alloy
  fi
elif command -v apt-get >/dev/null 2>&1; then
  # Ubuntu / Debian 계열
  mkdir -p /etc/apt/keyrings/ /etc/apt/sources.list.d/
  wget -q -O - https://apt.grafana.com/gpg.key | gpg --dearmor > /etc/apt/keyrings/grafana.gpg
  chmod 644 /etc/apt/keyrings/grafana.gpg
  echo "deb [signed-by=/etc/apt/keyrings/grafana.gpg] https://apt.grafana.com stable main" > /etc/apt/sources.list.d/grafana.list
  chmod 644 /etc/apt/sources.list.d/grafana.list
  apt-get update -qq
  apt-get install -y --no-install-recommends alloy
else
  echo "지원하지 않는 패키지 관리자입니다." >&2
  exit 1
fi

echo "=== 2. systemd 저널 보관 정책 적용 (200MB, 7일 보관) ==="
mkdir -p /etc/systemd/journald.conf.d
if [ -f "$JOURNALD_CONF_SRC" ]; then
  install -m 644 "$JOURNALD_CONF_SRC" /etc/systemd/journald.conf.d/tomo.conf
else
  cat <<'EOF' > /etc/systemd/journald.conf.d/tomo.conf
[Journal]
Storage=persistent
SystemMaxUse=200M
SystemMaxFileSize=50M
MaxRetentionSec=7day
RateLimitIntervalSec=30s
RateLimitBurst=10000
EOF
  chmod 644 /etc/systemd/journald.conf.d/tomo.conf
fi
install -d -o root -g systemd-journal -m 2755 /var/log/journal
systemctl restart systemd-journald
journalctl --flush

echo "=== 3. Alloy 권한 및 설정 배포 ==="
# Alloy가 systemd journal 로그를 읽을 수 있도록 그룹에 추가
usermod -aG systemd-journal alloy

# Alloy 환경변수 파일 준비 (/etc/alloy/alloy.env)
mkdir -p /etc/alloy
if [ ! -f /etc/alloy/alloy.env ]; then
  cat <<'EOF' > /etc/alloy/alloy.env
# Grafana Cloud Loki 연결 정보
GCLOUD_HOSTED_LOGS_URL="https://logs-prod-030.grafana.net/loki/api/v1/push"
GCLOUD_HOSTED_LOGS_ID="1849388"
GCLOUD_RW_API_KEY=""
EOF
  echo "⚠️  /etc/alloy/alloy.env 파일에 GCLOUD_RW_API_KEY(Grafana Cloud 토큰)를 입력해 주세요."
fi
chown root:alloy /etc/alloy/alloy.env
chmod 640 /etc/alloy/alloy.env

# systemd alloy 서비스에 EnvironmentFile 등록
mkdir -p /etc/systemd/system/alloy.service.d
cat <<'EOF' > /etc/systemd/system/alloy.service.d/override.conf
[Service]
EnvironmentFile=-/etc/alloy/alloy.env
EOF

# config.alloy 배포
if [ -f "$CONFIG_ALLOY_SRC" ]; then
  install -o root -g alloy -m 640 "$CONFIG_ALLOY_SRC" /etc/alloy/config.alloy
fi

echo "=== 4. Alloy 서비스 시작 및 등록 ==="
systemctl daemon-reload
systemctl enable alloy
systemctl restart alloy

echo "=== 5. 완료 및 상태 확인 ==="
systemctl is-active --quiet alloy && echo "✅ Grafana Alloy가 정상적으로 실행 중입니다." || echo "❌ Alloy 실행 상태를 확인해 주세요 (journalctl -u alloy -e)."
