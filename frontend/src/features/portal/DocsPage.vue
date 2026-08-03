<script setup lang="ts">
import { RouterLink } from 'vue-router'
import PortalHero from './components/PortalHero.vue'
import PortalSection from './components/PortalSection.vue'
import { docsSummary } from './data/content'

const hero = docsSummary.hero
const stats = docsSummary.stats
const sections = docsSummary.sections
const overviewIntro = docsSummary.overviewIntro
const quickStart = docsSummary.quickStart
</script>

<template>
  <div data-testid="portal-docs">
    <PortalHero
      :eyebrow="hero.eyebrow"
      :title="hero.title"
      :description="hero.description"
      solo
    >
      <template #details>
        <div class="portal-directory-list">
          <div class="portal-directory-item">
            <span class="portal-directory-label">版本</span>
            <p class="portal-directory-text">{{ hero.version }} · 更新于 {{ hero.updatedAt }}</p>
          </div>
          <div class="portal-directory-item">
            <span class="portal-directory-label">维护</span>
            <p class="portal-directory-text">{{ docsSummary.support.owner }}</p>
          </div>
        </div>
      </template>
      <template #bullets>
        <span v-for="role in hero.audience" :key="role" class="portal-chip">{{ role }}</span>
      </template>
      <template #actions>
        <RouterLink class="portal-btn portal-btn-secondary" to="/workspace">接入端简介</RouterLink>
        <RouterLink class="portal-btn portal-btn-primary" to="/login">登录后联调</RouterLink>
      </template>
    </PortalHero>

    <PortalSection
      eyebrow="Overview"
      title="文档中心简介"
      description="标准接入文档用于统一产品能力边界、业务域职责与联调步骤，降低评审与交接成本。"
    >
      <div class="portal-panel">
        <div class="portal-panel-inner portal-panel-stack">
          <p v-for="(para, idx) in overviewIntro" :key="idx" class="portal-muted-body">{{ para }}</p>
        </div>
      </div>
      <div class="portal-data-row" style="margin-top: 24px">
        <article v-for="item in stats" :key="item.label">
          <p class="portal-data-label">{{ item.label }}</p>
          <p class="portal-data-value">{{ item.value }}</p>
          <p class="portal-data-desc">{{ item.detail }}</p>
        </article>
      </div>
    </PortalSection>

    <PortalSection
      eyebrow="Chapters"
      title="章节覆盖"
      description="以下为当前 POC 文档目录的最小可用集，完整内容树可在后续迭代接入真实文档服务。"
    >
      <div class="portal-grid-2">
        <article v-for="section in sections" :key="section.id" class="portal-panel">
          <div class="portal-panel-inner">
            <div class="portal-panel-head">
              <h3>{{ section.title }}</h3>
              <span class="portal-chip portal-chip-accent">{{ section.badge }}</span>
            </div>
            <p class="portal-muted-body">{{ section.description }}</p>
          </div>
        </article>
      </div>
    </PortalSection>

    <PortalSection
      eyebrow="Quick Start"
      title="快速开始路径"
      description="通过演示环境完成本地启动、调试脚本与示例代码走查，预计 15 分钟建立基本接入认知。"
    >
      <div class="portal-timeline">
        <article v-for="(step, index) in quickStart" :key="step.id" class="portal-timeline-step">
          <span class="portal-number">{{ String(index + 1).padStart(2, '0') }}</span>
          <h3>{{ step.title }}</h3>
          <p>{{ step.description }}</p>
        </article>
      </div>
    </PortalSection>
  </div>
</template>
