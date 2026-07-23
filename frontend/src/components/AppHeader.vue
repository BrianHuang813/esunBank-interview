<script setup>
import { ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const menuOpen = ref(false)

watch(
  () => route.fullPath,
  () => {
    menuOpen.value = false
  },
)

async function logout() {
  authStore.clearSession()
  await router.push({ name: 'home' })
}
</script>

<template>
  <header class="site-header">
    <div class="site-header__inner">
      <RouterLink class="wordmark" :to="{ name: 'home' }">
        <span class="wordmark__mark" aria-hidden="true">玉</span>
        <span>玉山圖書室</span>
      </RouterLink>

      <button
        class="menu-toggle"
        type="button"
        :aria-expanded="menuOpen"
        aria-controls="primary-navigation"
        @click="menuOpen = !menuOpen"
      >
        {{ menuOpen ? '關閉' : '選單' }}
      </button>

      <nav
        id="primary-navigation"
        class="primary-nav"
        :class="{ 'primary-nav--open': menuOpen }"
        aria-label="主要導覽"
      >
        <RouterLink :to="{ name: 'home' }">館藏</RouterLink>
        <RouterLink v-if="authStore.isAuthenticated" :to="{ name: 'borrowings' }">
          我的借閱
        </RouterLink>
        <span v-if="authStore.user" class="primary-nav__user">
          {{ authStore.user.userName }}
        </span>
        <button
          v-if="authStore.isAuthenticated"
          class="text-action"
          type="button"
          @click="logout"
        >
          登出
        </button>
        <RouterLink v-else :to="{ name: 'login' }">登入</RouterLink>
      </nav>
    </div>
  </header>
</template>
