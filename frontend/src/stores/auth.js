import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import * as libraryApi from '../services/libraryApi'
import {
  clearSession as removeStoredSession,
  readSession,
  writeSession,
} from '../utils/session'

export const useAuthStore = defineStore('auth', () => {
  const session = ref(readSession())

  const isAuthenticated = computed(() => Boolean(session.value?.accessToken))
  const user = computed(() => session.value?.user ?? null)

  async function login(credentials) {
    const result = await libraryApi.login(credentials)
    session.value = {
      accessToken: result.accessToken,
      tokenType: result.tokenType,
      expiresIn: result.expiresIn,
      user: result.user,
    }
    writeSession(session.value)
    return result
  }

  function clearSession() {
    session.value = null
    removeStoredSession()
  }

  return {
    session,
    user,
    isAuthenticated,
    login,
    clearSession,
  }
})
