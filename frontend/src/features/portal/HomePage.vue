<script setup lang="ts">
import { RouterLink } from 'vue-router'
import PortalHero from './components/PortalHero.vue'
import PortalSection from './components/PortalSection.vue'
import { circulationSteps, siteSummary } from './data/content'

const hero = siteSummary.hero
const highlights = siteSummary.highlights
const domains = siteSummary.domains
const features = siteSummary.features
const scenarios = siteSummary.scenarios
const updates = siteSummary.updates
</script>

<template>
  <div data-testid="portal-home">
    <PortalHero :eyebrow="hero.eyebrow" :title="hero.title" :description="hero.description" solo>
      <template #details>
        <div class="portal-directory-list">
          <div class="portal-directory-item">
            <span class="portal-directory-label">入口</span>
            <p class="portal-directory-text">统一连接门户说明、标准文档与接入端工作台。</p>
          </div>
          <div class="portal-directory-item">
            <span class="portal-directory-label">重点</span>
            <p class="portal-directory-text">聚焦主体可信、目录发现、交易协同与履约清算。</p>
          </div>
        </div>
      </template>
    </PortalHero>

    <PortalSection
      eyebrow="Key Figures"
      title="门户总体态势"
      description="概览当前门户承载的主体规模、业务链路和整体接入成熟度，帮助合作伙伴快速判断方案覆盖范围。"
    >
      <template #leading>
        <div class="portal-arch-figure" role="img" aria-label="可信数据流通链总架构示意">
          <p>可信数据流通链 · 总架构示意</p>
        </div>
      </template>
      <div class="portal-data-row">
        <article v-for="item in highlights" :key="item.label">
          <p class="portal-data-label">{{ item.label }}</p>
          <p class="portal-data-value">{{ item.value }}</p>
          <p class="portal-data-desc">{{ item.description }}</p>
        </article>
      </div>
    </PortalSection>

    <PortalSection
      eyebrow="Business Line"
      title="数据流通链业务主线"
      description="按照业务推进顺序说明从可信接入到履约清算的完整链路，帮助生态伙伴理解各环节的职责与衔接关系。"
    >
      <div class="portal-timeline">
        <article v-for="(item, index) in circulationSteps" :key="item.title" class="portal-timeline-step">
          <span class="portal-number">{{ String(index + 1).padStart(2, '0') }}</span>
          <h3>{{ item.title }}</h3>
          <p>{{ item.detail }}</p>
        </article>
      </div>
    </PortalSection>

    <PortalSection
      eyebrow="Business Domains"
      title="四大业务环节"
      description="聚焦主体认证、目录发现、交易协同和履约清算四类核心能力，并提供对应的文档说明与工作台验证入口。"
    >
      <div class="portal-grid-2">
        <article v-for="domain in domains" :key="domain.id" class="portal-panel">
          <div class="portal-panel-inner">
            <div class="portal-panel-head">
              <div>
                <span class="portal-icon-badge">◇</span>
                <h3>{{ domain.title }}</h3>
              </div>
              <span class="portal-chip portal-chip-accent">{{ domain.metric }}</span>
            </div>
            <p class="portal-muted-body">{{ domain.summary }}</p>
            <div class="portal-chip-row">
              <span v-for="cap in domain.capabilities" :key="cap" class="portal-chip">{{ cap }}</span>
            </div>
            <div class="portal-panel-actions">
              <RouterLink class="portal-btn portal-btn-secondary" to="/docs">查看文档说明</RouterLink>
              <RouterLink class="portal-btn portal-btn-secondary" to="/workspace">去工作台验证</RouterLink>
            </div>
          </div>
        </article>
      </div>
    </PortalSection>

    <PortalSection
      eyebrow="Capability Overview"
      title="促进数据要素流通的核心特性"
      description="围绕关键能力和典型应用场景，说明数据流通链如何在真实业务中提升协同效率与流通可信度。"
    >
      <div class="portal-grid-2">
        <div class="portal-panel">
          <div class="portal-panel-inner portal-panel-stack">
            <article v-for="item in features" :key="item.title" class="portal-panel-muted portal-panel-inner">
              <h3 class="portal-muted-title">{{ item.title }}</h3>
              <p class="portal-muted-body">{{ item.description }}</p>
              <p class="portal-muted-detail">{{ item.detail }}</p>
            </article>
          </div>
        </div>
        <div class="portal-panel">
          <div class="portal-panel-inner portal-panel-stack">
            <article v-for="item in scenarios" :key="item.title" class="portal-panel-muted portal-panel-inner">
              <h3 class="portal-muted-title">{{ item.title }}</h3>
              <p class="portal-muted-body">{{ item.description }}</p>
              <div class="portal-chip-row">
                <span class="portal-chip">{{ item.role }}</span>
                <span class="portal-chip portal-chip-accent">{{ item.outcome }}</span>
              </div>
            </article>
          </div>
        </div>
      </div>
    </PortalSection>

    <PortalSection
      eyebrow="Updates & Actions"
      title="新闻公告与继续了解"
      description="集中呈现门户动态、版本信息和下一步进入路径，方便合作伙伴持续跟踪方案进展并进入详细内容。"
    >
      <div class="portal-grid-2">
        <div class="portal-panel">
          <div class="portal-panel-inner portal-panel-stack">
            <article v-for="item in updates" :key="item.title" class="portal-panel-muted portal-panel-inner">
              <div class="portal-chip-row" style="margin-top: 0">
                <span class="portal-chip portal-chip-accent">{{ item.type }}</span>
                <span class="portal-data-label">{{ item.date }}</span>
              </div>
              <h3 class="portal-muted-title" style="margin-top: 16px">{{ item.title }}</h3>
              <p class="portal-muted-body">{{ item.summary }}</p>
            </article>
          </div>
        </div>
        <div class="portal-grid-2">
          <RouterLink class="portal-link-card" to="/docs">
            <div>
              <span class="portal-icon-badge">文</span>
              <h3>进入标准接入文档</h3>
              <p>查看标准接入文档简介，了解章节覆盖与快速开始路径。</p>
            </div>
            <div class="portal-link-cta">打开入口 →</div>
          </RouterLink>
          <RouterLink class="portal-link-card" to="/workspace">
            <div>
              <span class="portal-icon-badge">台</span>
              <h3>进入接入端简介</h3>
              <p>了解接入端工作台承载的业务能力，并从简介页进入登录。</p>
            </div>
            <div class="portal-link-cta">打开入口 →</div>
          </RouterLink>
        </div>
      </div>
    </PortalSection>
  </div>
</template>
