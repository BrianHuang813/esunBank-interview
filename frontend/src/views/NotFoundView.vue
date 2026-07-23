<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <main class="page page--not-found">
    <p class="section-kicker">找不到頁面</p>
    <h1>這個位置沒有內容</h1>
    <p>網址可能已變更，或頁面目前不存在。</p>
    <RouterLink class="button button--secondary" :to="{ name: 'home' }">
      回到館藏
    </RouterLink>
  </main>
</template>
