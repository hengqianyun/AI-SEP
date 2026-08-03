<script setup lang="ts">
defineProps<{
  title: string
  eyebrow?: string
  description?: string
}>()
</script>

<template>
  <section class="portal-section" data-testid="portal-section">
    <div v-if="$slots.leading" class="portal-leading">
      <slot name="leading" />
    </div>
    <div class="portal-section-header">
      <div>
        <p v-if="eyebrow" class="portal-kicker">{{ eyebrow }}</p>
        <h2 class="portal-section-title">{{ title }}</h2>
        <p v-if="description" class="portal-section-desc">{{ description }}</p>
      </div>
      <slot name="aside" />
    </div>
    <slot />
  </section>
</template>
