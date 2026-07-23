<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import BookCover from '../components/BookCover.vue'
import LoadingBlock from '../components/LoadingBlock.vue'
import StatePanel from '../components/StatePanel.vue'
import { apiErrorMessage } from '../services/api'
import { borrowInventory, getBook } from '../services/libraryApi'
import { useAuthStore } from '../stores/auth'
import { formatDateTime, inventoryStatusLabel } from '../utils/formatters'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const book = ref(null)
const loading = ref(true)
const errorMessage = ref('')
const actionError = ref('')
const feedback = ref('')
const borrowingInventoryId = ref(null)

const availableInventory = computed(
  () => book.value?.inventory.filter((item) => item.status === 'AVAILABLE') ?? [],
)

async function loadBook() {
  loading.value = true
  errorMessage.value = ''
  feedback.value = ''

  try {
    book.value = await getBook(route.params.isbn)
  } catch (error) {
    errorMessage.value = apiErrorMessage(error, '目前無法取得書籍資料')
  } finally {
    loading.value = false
  }
}

async function borrow(item) {
  actionError.value = ''
  feedback.value = ''

  if (!authStore.isAuthenticated) {
    await router.push({
      name: 'login',
      query: { redirect: route.fullPath },
    })
    return
  }

  borrowingInventoryId.value = item.inventoryId
  try {
    await borrowInventory(item.inventoryId)
    feedback.value = `已借閱館藏編號 ${item.inventoryId}`
    await loadBook()
    feedback.value = `已借閱館藏編號 ${item.inventoryId}`
  } catch (error) {
    actionError.value = apiErrorMessage(error, '目前無法完成借閱')
    if (error.response?.status === 409) {
      await loadBook()
      actionError.value = '這本館藏剛剛已被借走，狀態已為你更新。'
    }
  } finally {
    borrowingInventoryId.value = null
  }
}

watch(() => route.params.isbn, loadBook)
onMounted(loadBook)
</script>

<template>
  <main class="page page--detail">
    <RouterLink class="back-link" :to="{ name: 'home' }">返回館藏</RouterLink>

    <LoadingBlock v-if="loading" label="書籍資料載入中" :rows="1" />

    <StatePanel
      v-else-if="errorMessage"
      title="無法顯示這本書"
      :message="errorMessage"
    >
      <button class="button button--secondary" type="button" @click="loadBook">
        重新載入
      </button>
    </StatePanel>

    <template v-else-if="book">
      <section class="book-detail">
        <div class="book-detail__cover">
          <BookCover
            :isbn="book.isbn"
            :title="book.name"
            :author="book.author"
            size="large"
          />
        </div>

        <div class="book-detail__copy">
          <p class="book-detail__author">{{ book.author }}</p>
          <h1>{{ book.name }}</h1>
          <p class="book-detail__isbn">ISBN {{ book.isbn }}</p>
          <p class="book-detail__introduction">{{ book.introduction }}</p>
        </div>

        <aside class="availability-summary">
          <span class="availability-summary__number">{{ book.availableCount }}</span>
          <span>本可借</span>
          <span>總館藏 {{ book.inventoryCount }} 本</span>
        </aside>
      </section>

      <section class="inventory-section">
        <div class="inventory-section__heading">
          <h2>館藏狀態</h2>
          <p v-if="availableInventory.length">
            選擇可借閱的館藏副本完成借閱。
          </p>
          <p v-else>目前沒有可借閱的副本。</p>
        </div>

        <p v-if="feedback" class="feedback feedback--success" role="status">
          {{ feedback }}
          <RouterLink class="text-link" :to="{ name: 'borrowings' }">查看我的借閱</RouterLink>
        </p>
        <p v-if="actionError" class="feedback feedback--error" role="alert">
          {{ actionError }}
        </p>

        <div class="inventory-list">
          <article
            v-for="item in book.inventory"
            :key="item.inventoryId"
            class="inventory-row"
          >
            <div>
              <span class="inventory-row__id">館藏編號 {{ item.inventoryId }}</span>
              <span
                class="status-text"
                :class="{ 'status-text--muted': item.status !== 'AVAILABLE' }"
              >
                {{ inventoryStatusLabel(item.status) }}
              </span>
            </div>
            <span class="inventory-row__date">
              入館 {{ formatDateTime(item.storeTime) }}
            </span>
            <button
              v-if="item.status === 'AVAILABLE'"
              class="button"
              type="button"
              :disabled="borrowingInventoryId !== null"
              @click="borrow(item)"
            >
              {{ borrowingInventoryId === item.inventoryId ? '借閱處理中' : '借閱此副本' }}
            </button>
            <span v-else class="inventory-row__unavailable">目前不可借閱</span>
          </article>
        </div>
      </section>
    </template>
  </main>
</template>
