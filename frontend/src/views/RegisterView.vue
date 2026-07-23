<script setup>
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

import { apiErrorMessage, apiFieldErrors } from '../services/api'
import { register } from '../services/libraryApi'
import {
  validatePassword,
  validatePhoneNumber,
  validateUserName,
} from '../utils/validation'

const router = useRouter()
const form = reactive({
  userName: '',
  phoneNumber: '',
  password: '',
})
const errors = reactive({
  userName: '',
  phoneNumber: '',
  password: '',
})
const submitting = ref(false)
const submitError = ref('')

function validate() {
  errors.userName = validateUserName(form.userName)
  errors.phoneNumber = validatePhoneNumber(form.phoneNumber)
  errors.password = validatePassword(form.password)
  return !errors.userName && !errors.phoneNumber && !errors.password
}

async function submit() {
  submitError.value = ''
  if (!validate()) {
    return
  }

  submitting.value = true
  try {
    await register({
      userName: form.userName.trim(),
      phoneNumber: form.phoneNumber,
      password: form.password,
    })
    await router.replace({ name: 'login', query: { registered: '1' } })
  } catch (error) {
    Object.assign(errors, apiFieldErrors(error))
    submitError.value = apiErrorMessage(error, '目前無法完成註冊')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="page auth-page auth-page--register">
    <section class="auth-panel">
      <p class="section-kicker">建立帳號</p>
      <h1>開始你的閱讀之旅</h1>
      <p class="auth-panel__lead">使用手機號碼註冊，密碼長度需為 8 至 20 個字元</p>

      <p v-if="submitError" class="feedback feedback--error" role="alert">
        {{ submitError }}
      </p>

      <form class="form-stack" novalidate @submit.prevent="submit">
        <div class="field">
          <label for="register-name">姓名</label>
          <input
            id="register-name"
            v-model="form.userName"
            type="text"
            name="userName"
            autocomplete="name"
            maxlength="30"
            placeholder="輸入姓名"
            :aria-invalid="Boolean(errors.userName)"
            :aria-describedby="errors.userName ? 'register-name-error' : undefined"
            @blur="errors.userName = validateUserName(form.userName)"
          />
          <p class="field__hint">僅能使用中文、英文字母與空格。</p>
          <p v-if="errors.userName" id="register-name-error" class="field__error">
            {{ errors.userName }}
          </p>
        </div>

        <div class="field">
          <label for="register-phone">手機號碼</label>
          <input
            id="register-phone"
            v-model.trim="form.phoneNumber"
            type="tel"
            name="phoneNumber"
            inputmode="numeric"
            autocomplete="tel"
            maxlength="10"
            placeholder="0912345678"
            :aria-invalid="Boolean(errors.phoneNumber)"
            :aria-describedby="errors.phoneNumber ? 'register-phone-error' : undefined"
            @blur="errors.phoneNumber = validatePhoneNumber(form.phoneNumber)"
          />
          <p v-if="errors.phoneNumber" id="register-phone-error" class="field__error">
            {{ errors.phoneNumber }}
          </p>
        </div>

        <div class="field">
          <label for="register-password">密碼</label>
          <input
            id="register-password"
            v-model="form.password"
            type="password"
            name="password"
            autocomplete="new-password"
            maxlength="20"
            placeholder="8 至 20 個字元"
            :aria-invalid="Boolean(errors.password)"
            :aria-describedby="errors.password ? 'register-password-error' : undefined"
            @blur="errors.password = validatePassword(form.password)"
          />
          <p class="field__hint">建議混合英文、數字與符號，請勿使用其他網站相同密碼。</p>
          <p v-if="errors.password" id="register-password-error" class="field__error">
            {{ errors.password }}
          </p>
        </div>

        <button class="button form-stack__submit" type="submit" :disabled="submitting">
          {{ submitting ? '帳號建立中' : '建立帳號' }}
        </button>
      </form>

      <p class="auth-panel__alternate">
        已經有帳號？
        <RouterLink class="text-link" :to="{ name: 'login' }">前往登入</RouterLink>
      </p>
    </section>
  </main>
</template>
