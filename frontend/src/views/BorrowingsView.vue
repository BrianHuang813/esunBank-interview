<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import BookCover from '../components/BookCover.vue'
import LoadingBlock from '../components/LoadingBlock.vue'
import PaginationControls from '../components/PaginationControls.vue'
import StatePanel from '../components/StatePanel.vue'
import { apiErrorMessage } from '../services/api'
import { getMyBorrowings, returnBorrowing } from '../services/libraryApi'
import { borrowingStatusLabel, formatDateTime } from '../utils/formatters'

const PAGE_SIZE = 10
const FILTERS = [
  { value: 'ACTIVE', label: '借閱中' },
  { value: 'RETURNED', label: '已歸還' },
  { value: 'ALL', label: '全部紀錄' },
]

const route = useRoute()
const router = useRouter()
const result = ref(null)
const loading = ref(true)
const errorMessage = ref('')
const actionError = ref('')
const feedback = ref('')
const pendingReturnId = ref(null)
const returningId = ref(null)

function currentStatus() {
  const status = typeof route.query.status === 'string' ? route.query.status.toUpperCase() : 'ACTIVE'
  return FILTERS.some((filter) => filter.value === status) ? status : 'ACTIVE'
}

function currentPage() {
  const page = Number.parseInt(route.query.page, 10)
  return Number.isInteger(page) && page >= 0 ? page : 0
}

async function loadBorrowings() {
  loading.value = true
  errorMessage.value = ''

  try {
    result.value = await getMyBorrowings({
      status: currentStatus(),
      page: currentPage(),
      size: PAGE_SIZE,
    })
  } catch (error) {
    errorMessage.value = apiErrorMessage(error, '目前無法取得借閱紀錄')
  } finally {
    loading.value = false
  }
}

async function selectFilter(status) {
  feedback.value = ''
  actionError.value = ''
  pendingReturnId.value = null
  await router.push({
    name: 'borrowings',
    query: status === 'ACTIVE' ? {} : { status },
  })
  await loadBorrowings()
}

async function changePage(page) {
  await router.push({
    name: 'borrowings',
    query: {
      ...(currentStatus() === 'ACTIVE' ? {} : { status: currentStatus() }),
      page: String(page),
    },
  })
  await loadBorrowings()
  document.querySelector('main')?.focus()
}

async function confirmReturn(record) {
  returningId.value = record.id
  actionError.value = ''
  feedback.value = ''

  try {
    await returnBorrowing(record.id)
    feedback.value = `《${record.bookName}》已完成歸還`
    pendingReturnId.value = null
    await loadBorrowings()
  } catch (error) {
    actionError.value = apiErrorMessage(error, '目前無法完成歸還')
  } finally {
    returningId.value = null
  }
}

onMounted(loadBorrowings)
</script>

<template>
  <main class="page page--borrowings" tabindex="-1">
    <section class="borrowings-heading">
      <div>
        <p class="section-kicker">個人借閱</p>
        <h1>我的閱讀紀錄</h1>
      </div>
      <p>查看正在借閱的館藏，或回顧已完成的歸還紀錄。</p>
    </section>

    <nav class="filter-nav" aria-label="借閱紀錄篩選">
      <button
        v-for="filter in FILTERS"
        :key="filter.value"
        type="button"
        :class="{ 'filter-nav__active': currentStatus() === filter.value }"
        @click="selectFilter(filter.value)"
      >
        {{ filter.label }}
      </button>
    </nav>

    <p v-if="feedback" class="feedback feedback--success" role="status">
      {{ feedback }}
    </p>
    <p v-if="actionError" class="feedback feedback--error" role="alert">
      {{ actionError }}
    </p>

    <LoadingBlock v-if="loading" label="借閱紀錄載入中" :rows="2" />

    <StatePanel
      v-else-if="errorMessage"
      title="借閱紀錄暫時無法載入"
      :message="errorMessage"
    >
      <button class="button button--secondary" type="button" @click="loadBorrowings">
        重新載入
      </button>
    </StatePanel>

    <StatePanel
      v-else-if="result?.content.length === 0"
      :title="currentStatus() === 'ACTIVE' ? '目前沒有借閱中的館藏' : '目前沒有符合的紀錄'"
      :message="
        currentStatus() === 'ACTIVE'
          ? '從館藏中找到想閱讀的書，就可以開始第一筆借閱。'
          : '切換其他分類，或回到館藏開始借閱。'
      "
    >
      <RouterLink class="button button--secondary" :to="{ name: 'home' }">
        前往館藏
      </RouterLink>
    </StatePanel>

    <section v-else class="borrowing-list" aria-label="借閱紀錄">
      <article
        v-for="record in result.content"
        :key="record.id"
        class="borrowing-row"
        :class="{ 'borrowing-row--returned': record.returnTime }"
      >
        <RouterLink
          class="borrowing-row__cover"
          :to="{ name: 'book-detail', params: { isbn: record.isbn } }"
          :aria-label="`查看${record.bookName}`"
        >
          <BookCover
            :isbn="record.isbn"
            :title="record.bookName"
            :author="record.author"
            size="small"
          />
        </RouterLink>

        <div class="borrowing-row__book">
          <span class="status-text" :class="{ 'status-text--muted': record.returnTime }">
            {{ borrowingStatusLabel(record) }}
          </span>
          <h2>
            <RouterLink :to="{ name: 'book-detail', params: { isbn: record.isbn } }">
              {{ record.bookName }}
            </RouterLink>
          </h2>
          <p>{{ record.author }}</p>
          <p class="borrowing-row__inventory">館藏編號 {{ record.inventoryId }}</p>
        </div>

        <dl class="borrowing-row__dates">
          <div>
            <dt>借閱時間</dt>
            <dd>{{ formatDateTime(record.borrowingTime) }}</dd>
          </div>
          <div>
            <dt>歸還時間</dt>
            <dd>{{ record.returnTime ? formatDateTime(record.returnTime) : '尚未歸還' }}</dd>
          </div>
        </dl>

        <div v-if="!record.returnTime" class="borrowing-row__action">
          <template v-if="pendingReturnId === record.id">
            <p>確認歸還這本書？</p>
            <div>
              <button
                class="button"
                type="button"
                :disabled="returningId === record.id"
                @click="confirmReturn(record)"
              >
                {{ returningId === record.id ? '歸還處理中' : '確認歸還' }}
              </button>
              <button
                class="text-action"
                type="button"
                :disabled="returningId === record.id"
                @click="pendingReturnId = null"
              >
                取消
              </button>
            </div>
          </template>
          <button
            v-else
            class="button button--secondary"
            type="button"
            @click="pendingReturnId = record.id"
          >
            歸還
          </button>
        </div>
      </article>
    </section>

    <PaginationControls
      v-if="result"
      :page="result.page"
      :total-pages="result.totalPages"
      @change="changePage"
    />
  </main>
</template>
