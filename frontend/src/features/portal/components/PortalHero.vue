<script setup lang="ts">
defineProps<{
  eyebrow: string
  title: string
  description: string
  solo?: boolean
}>()
</script>

<template>
  <section class="portal-hero" data-testid="portal-hero">
    <div class="portal-hero-grid" :class="{ solo }">
      <div>
        <p class="portal-kicker">{{ eyebrow }}</p>
        <h1 class="portal-title">{{ title }}</h1>
        <p class="portal-lead">{{ description }}</p>
        <slot name="details" />
        <div v-if="$slots.bullets" class="portal-bullets">
          <slot name="bullets" />
        </div>
        <div v-if="$slots.actions" class="portal-actions">
          <slot name="actions" />
        </div>
      </div>
      <aside v-if="$slots.aside" class="portal-aside-panel">
        <slot name="aside" />
      </aside>
    </div>
  </section>
</template>
