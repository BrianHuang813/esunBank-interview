<script setup>
const props = defineProps({
  page: {
    type: Number,
    required: true,
  },
  totalPages: {
    type: Number,
    required: true,
  },
})

const emit = defineEmits(['change'])

function goTo(page) {
  if (page >= 0 && page < props.totalPages && page !== props.page) {
    emit('change', page)
  }
}
</script>

<template>
  <nav v-if="totalPages > 1" class="pagination" aria-label="分頁">
    <button
      class="button button--secondary"
      type="button"
      :disabled="page === 0"
      @click="goTo(page - 1)"
    >
      上一頁
    </button>
    <span>第 {{ page + 1 }} 頁，共 {{ totalPages }} 頁</span>
    <button
      class="button button--secondary"
      type="button"
      :disabled="page >= totalPages - 1"
      @click="goTo(page + 1)"
    >
      下一頁
    </button>
  </nav>
</template>
