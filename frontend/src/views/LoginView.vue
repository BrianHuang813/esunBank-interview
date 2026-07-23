<script setup>
import { reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { apiErrorMessage } from '../services/api'
import { useAuthStore } from '../stores/auth'
import { validatePassword, validatePhoneNumber } from '../utils/validation'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  phoneNumber: '',
  password: '',
})
const errors = reactive({
  phoneNumber: '',
  password: '',
})
const submitting = ref(false)
const submitError = ref('')

function validate() {
  errors.phoneNumber = validatePhoneNumber(form.phoneNumber)
  errors.password = validatePassword(form.password)
  return !errors.phoneNumber && !errors.password
}

async function submit() {
  submitError.value = ''
  if (!validate()) {
    return
  }

  submitting.value = true
  try {
    await authStore.login(form)
    const redirect =
      typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/')
        ? route.query.redirect
        : '/'
    await router.replace(redirect)
  } catch (error) {
    submitError.value = apiErrorMessage(error, '手機號碼或密碼不正確')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="page auth-page">
    <section class="auth-panel">
      <p class="section-kicker">會員登入</p>
      <h1>回到你的閱讀紀錄</h1>
      <p class="auth-panel__lead">登入後可以借閱館藏、查看借閱中項目並完成歸還。</p>

      <p v-if="route.query.registered" class="feedback feedback--success" role="status">
        註冊完成，請使用手機號碼登入。
      </p>
      <p v-if="route.query.expired" class="feedback feedback--error" role="alert">
        登入狀態已失效，請重新登入。
      </p>
      <p v-if="submitError" class="feedback feedback--error" role="alert">
        {{ submitError }}
      </p>

      <form class="form-stack" novalidate @submit.prevent="submit">
        <div class="field">
          <label for="login-phone">手機號碼</label>
          <input
            id="login-phone"
            v-model.trim="form.phoneNumber"
            type="tel"
            name="phoneNumber"
            inputmode="numeric"
            autocomplete="tel"
            maxlength="10"
            placeholder="0912345678"
            :aria-invalid="Boolean(errors.phoneNumber)"
            :aria-describedby="errors.phoneNumber ? 'login-phone-error' : undefined"
            @blur="errors.phoneNumber = validatePhoneNumber(form.phoneNumber)"
          />
          <p v-if="errors.phoneNumber" id="login-phone-error" class="field__error">
            {{ errors.phoneNumber }}
          </p>
        </div>

        <div class="field">
          <label for="login-password">密碼</label>
          <input
            id="login-password"
            v-model="form.password"
            type="password"
            name="password"
            autocomplete="current-password"
            maxlength="20"
            placeholder="輸入密碼"
            :aria-invalid="Boolean(errors.password)"
            :aria-describedby="errors.password ? 'login-password-error' : undefined"
            @blur="errors.password = validatePassword(form.password)"
          />
          <p v-if="errors.password" id="login-password-error" class="field__error">
            {{ errors.password }}
          </p>
        </div>

        <button class="button form-stack__submit" type="submit" :disabled="submitting">
          {{ submitting ? '登入處理中' : '登入' }}
        </button>
      </form>

      <p class="auth-panel__alternate">
        還沒有帳號？
        <RouterLink class="text-link" :to="{ name: 'register' }">建立帳號</RouterLink>
      </p>
    </section>

    <aside class="auth-note">
      <h2>借閱從館藏開始</h2>
      <p>館藏頁可以直接查看每一本書的可借數量，不需登入即可瀏覽。</p>
      <RouterLink class="text-link" :to="{ name: 'home' }">先看看館藏</RouterLink>
    </aside>
  </main>
</template>
