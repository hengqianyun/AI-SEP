# syntax=docker/dockerfile:1.7

FROM node:20-alpine AS build
WORKDIR /workspace

RUN corepack enable \
    && corepack prepare pnpm@9.15.0 --activate

COPY package.json pnpm-lock.yaml ./
RUN --mount=type=cache,target=/root/.local/share/pnpm/store \
    pnpm install --frozen-lockfile

COPY index.html ./
COPY tsconfig.json tsconfig.app.json tsconfig.node.json vite.config.ts ./
COPY src ./src

ARG VITE_API_BASE_URL=/api/v1
ARG VITE_BASE=/
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_BASE=${VITE_BASE}

RUN pnpm build

FROM nginx:1.27-alpine
COPY --from=build /workspace/dist /usr/share/nginx/html

EXPOSE 80

