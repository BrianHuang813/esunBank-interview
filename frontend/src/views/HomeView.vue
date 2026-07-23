<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import BookCover from '../components/BookCover.vue'
import LoadingBlock from '../components/LoadingBlock.vue'
import PaginationControls from '../components/PaginationControls.vue'
import StatePanel from '../components/StatePanel.vue'
import { apiErrorMessage } from '../services/api'
import { getBooks } from '../services/libraryApi'

const PAGE_SIZE = 8

const route = useRoute()
const router = useRouter()
const keyword = ref(typeof route.query.q === 'string' ? route.query.q : '')
const result = ref(null)
const loading = ref(true)
const errorMessage = ref('')
let requestSequence = 0

function routePage() {
  const parsed = Number.parseInt(route.query.page, 10)
  return Number.isInteger(parsed) && parsed >= 0 ? parsed : 0
}

async function loadBooks() {
  const sequence = ++requestSequence
  loading.value = true
  errorMessage.value = ''

  try {
    const data = await getBooks({
      keyword: typeof route.query.q === 'string' ? route.query.q : '',
      page: routePage(),
      size: PAGE_SIZE,
    })
    if (sequence === requestSequence) {
      result.value = data
    }
  } catch (error) {
    if (sequence === requestSequence) {
      errorMessage.value = apiErrorMessage(error, '目前無法取得館藏')
    }
  } finally {
    if (sequence === requestSequence) {
      loading.value = false
    }
  }
}

async function search() {
  const query = keyword.value.trim()
  await router.push({
    name: 'home',
    query: query ? { q: query } : {},
  })
  await loadBooks()
}

async function clearSearch() {
  keyword.value = ''
  await router.push({ name: 'home' })
  await loadBooks()
}

async function changePage(page) {
  const query = { ...route.query, page: String(page) }
  if (!query.q) {
    delete query.q
  }
  await router.push({ name: 'home', query })
  await loadBooks()
  document.querySelector('main')?.focus()
}

onMounted(loadBooks)
</script>

<template>
  <main class="page page--catalog" tabindex="-1">
    <section class="catalog-intro">
      <div>
        <p class="section-kicker">線上館藏</p>
        <h1>找到下一本要讀的書</h1>
        <p class="catalog-intro__summary">
          查看即時館藏狀態，登入後即可借閱可用副本。
        </p>
      </div>

      <form class="search-form" role="search" @submit.prevent="search">
        <label for="book-search">搜尋館藏</label>
        <div class="search-form__controls">
          <input
            id="book-search"
            v-model="keyword"
            type="search"
            name="q"
            autocomplete="off"
            maxlength="255"
            placeholder="書名、作者或 ISBN"
          />
          <button class="button" type="submit">搜尋</button>
        </div>
        <button
          v-if="route.query.q"
          class="text-action search-form__clear"
          type="button"
          @click="clearSearch"
        >
          清除搜尋
        </button>
      </form>
    </section>

    <div class="catalog-meta" aria-live="polite">
      <p v-if="result && !loading">
        <template v-if="route.query.q">
          「{{ route.query.q }}」找到 {{ result.totalElements }} 筆書目
        </template>
        <template v-else>目前共有 {{ result.totalElements }} 筆書目</template>
      </p>
    </div>

    <LoadingBlock v-if="loading" label="館藏載入中" :rows="3" />

    <StatePanel
      v-else-if="errorMessage"
      title="館藏暫時無法載入"
      :message="errorMessage"
    >
      <button class="button button--secondary" type="button" @click="loadBooks">
        重新載入
      </button>
    </StatePanel>

    <StatePanel
      v-else-if="result?.content.length === 0"
      title="沒有符合條件的書目"
      message="可以換一個書名、作者或 ISBN 再搜尋。"
    >
      <button class="button button--secondary" type="button" @click="clearSearch">
        查看全部館藏
      </button>
    </StatePanel>

    <section v-else class="book-index" aria-label="書目列表">
      <article
        v-for="(book, index) in result.content"
        :key="book.isbn"
        class="book-row"
        :class="`book-row--offset-${(index % 3) + 1}`"
      >
        <RouterLink
          class="book-row__cover-link"
          :to="{ name: 'book-detail', params: { isbn: book.isbn } }"
          :aria-label="`查看${book.name}`"
        >
          <BookCover
            :isbn="book.isbn"
            :title="book.name"
            :author="book.author"
            size="medium"
          />
        </RouterLink>

        <div class="book-row__body">
          <div>
            <p class="book-row__author">{{ book.author }}</p>
            <h2>
              <RouterLink :to="{ name: 'book-detail', params: { isbn: book.isbn } }">
                {{ book.name }}
              </RouterLink>
            </h2>
            <p class="book-row__introduction">{{ book.introduction }}</p>
          </div>

          <div class="book-row__availability">
            <span :class="{ 'status-text--muted': book.availableCount === 0 }">
              {{ book.availableCount > 0 ? `可借 ${book.availableCount} 本` : '目前無可借副本' }}
            </span>
            <span>館藏 {{ book.inventoryCount }} 本</span>
            <RouterLink
              class="text-link"
              :to="{ name: 'book-detail', params: { isbn: book.isbn } }"
            >
              查看館藏
            </RouterLink>
          </div>
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
