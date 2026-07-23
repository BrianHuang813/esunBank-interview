<script setup>
import { onBeforeUnmount, onMounted } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'

import AppHeader from './components/AppHeader.vue'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

function handleExpiredSession() {
  authStore.clearSession()
  if (route.meta.requiresAuth) {
    router.replace({
      name: 'login',
      query: { redirect: route.fullPath, expired: '1' },
    })
  }
}

onMounted(() => {
  window.addEventListener('auth:expired', handleExpiredSession)
})

onBeforeUnmount(() => {
  window.removeEventListener('auth:expired', handleExpiredSession)
})
</script>

<template>
  <div class="app-frame">
    <AppHeader />
    <RouterView />
  </div>
</template>
