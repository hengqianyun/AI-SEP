<script setup lang="ts">
import { RouterLink } from 'vue-router'
import PortalHero from './components/PortalHero.vue'
import PortalSection from './components/PortalSection.vue'
import { workspaceIntro } from './data/content'

const session = workspaceIntro.session
const stats = workspaceIntro.stats
const menus = workspaceIntro.menus
const demos = workspaceIntro.demos
</script>

<template>
  <div data-testid="portal-workspace">
    <PortalHero
      eyebrow="接入端工作台"
      title="面向生态伙伴的数据流通协同控制台"
      description="接入端工作台承载总览驾驶舱、身份凭证、目录订单与清算设置等能力，帮助已认证主体在统一界面完成业务验证与运营跟踪。"
      solo
    >
      <template #details>
        <div class="portal-directory-list">
          <div class="portal-directory-item">
            <span class="portal-directory-label">企业</span>
            <p class="portal-directory-text">{{ session.enterpriseName }}</p>
          </div>
          <div class="portal-directory-item">
            <span class="portal-directory-label">角色</span>
            <p class="portal-directory-text">{{ session.role }} · {{ session.environment }}</p>
          </div>
          <div class="portal-directory-item">
            <span class="portal-directory-label">状态</span>
            <p class="portal-directory-text">{{ session.accessStatus }}</p>
          </div>
        </div>
      </template>
      <template #actions>
        <RouterLink class="portal-btn portal-btn-primary" to="/login" data-testid="open-console-link">
          打开接入端
        </RouterLink>
        <RouterLink class="portal-btn portal-btn-secondary" to="/docs">查看接入文档</RouterLink>
      </template>
    </PortalHero>

    <PortalSection
      eyebrow="Workspace Snapshot"
      title="工作台概览指标"
      description="以下为 POC 演示环境的静态快照数据，展示接入端在认证、目录与订单维度的典型运营视图。"
    >
      <div class="portal-data-row">
        <article v-for="item in stats" :key="item.id">
          <p class="portal-data-label">{{ item.label }}</p>
          <p class="portal-data-value">{{ item.value }}</p>
          <p class="portal-data-desc">{{ item.trend }}</p>
        </article>
      </div>
    </PortalSection>

    <PortalSection
      eyebrow="Capability Map"
      title="工作台模块导航"
      description="接入端按业务域划分功能模块，登录后可进入对应页面完成目录筛选、订单协同与清算跟踪。"
    >
      <div class="portal-grid-2">
        <article v-for="item in menus" :key="item.label" class="portal-panel">
          <div class="portal-panel-inner">
            <span class="portal-icon-badge">◆</span>
            <h3>{{ item.label }}</h3>
            <p class="portal-muted-body">{{ item.description }}</p>
          </div>
        </article>
      </div>
    </PortalSection>

    <PortalSection
      eyebrow="Demo Scenarios"
      title="可验证的演示场景"
      description="以下场景可在登录后于工作台中体验，用于验证目录发现、订单协同与管理配置等关键路径。"
    >
      <div class="portal-grid-2">
        <article v-for="item in demos" :key="item.label" class="portal-panel-muted portal-panel-inner">
          <h3 class="portal-muted-title">{{ item.label }}</h3>
          <p class="portal-muted-body">{{ item.description }}</p>
        </article>
      </div>
      <div class="portal-actions" style="margin-top: 24px">
        <RouterLink class="portal-btn portal-btn-primary" to="/login" data-testid="open-console-cta">
          打开接入端
        </RouterLink>
      </div>
    </PortalSection>
  </div>
</template>
