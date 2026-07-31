#!/usr/bin/env bash

set -e

BASE_DIR="/app/datachain"
BACKEND_DIR="${BASE_DIR}/data-chain-backend"
FRONTEND_DIR="${BASE_DIR}/data-chain-static"

BACKEND_BRANCH="master"
FRONTEND_BRANCH="master"
HTTP_PORT="18080"
PUBLIC_ORIGIN="http://YOUR_SERVER_IP:${HTTP_PORT}"

git -C "$BACKEND_DIR" pull --ff-only origin "$BACKEND_BRANCH"
git -C "$FRONTEND_DIR" pull --ff-only origin "$FRONTEND_BRANCH"

cat >"${BACKEND_DIR}/deploy/.env" <<EOF
COMPOSE_PROJECT_NAME=data-chain-demo
IMAGE_TAG=demo
FRONTEND_BUILD_CONTEXT=../../data-chain-static
FRONTEND_DOCKERFILE=../data-chain-backend/deploy/frontend.Dockerfile
HTTP_PORT=${HTTP_PORT}
WSC_CORS_ALLOWED_ORIGINS=${PUBLIC_ORIGIN}
TZ=Asia/Shanghai
EOF

docker compose \
  --env-file "${BACKEND_DIR}/deploy/.env" \
  -f "${BACKEND_DIR}/deploy/compose.yml" \
  up -d --build

docker compose \
  --env-file "${BACKEND_DIR}/deploy/.env" \
  -f "${BACKEND_DIR}/deploy/compose.yml" \
  ps

echo "部署完成：${PUBLIC_ORIGIN}"
