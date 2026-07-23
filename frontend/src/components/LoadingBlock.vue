<script setup>
defineProps({
  label: {
    type: String,
    default: '資料載入中',
  },
  rows: {
    type: Number,
    default: 3,
  },
})
</script>

<template>
  <section class="loading-block" aria-live="polite" aria-busy="true">
    <p class="sr-only">{{ label }}</p>
    <div v-for="index in rows" :key="index" class="loading-row" aria-hidden="true">
      <span class="loading-row__media"></span>
      <span class="loading-row__copy">
        <span></span>
        <span></span>
        <span></span>
      </span>
    </div>
  </section>
</template>
