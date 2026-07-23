<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  isbn: {
    type: String,
    required: true,
  },
  title: {
    type: String,
    required: true,
  },
  author: {
    type: String,
    default: '',
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value),
  },
})

const imageFailed = ref(false)
const imageUrl = computed(
  () => `https://covers.openlibrary.org/b/isbn/${props.isbn}-L.jpg?default=false`,
)

watch(
  () => props.isbn,
  () => {
    imageFailed.value = false
  },
)
</script>

<template>
  <div class="book-cover" :class="`book-cover--${size}`">
    <img
      v-if="!imageFailed"
      :src="imageUrl"
      :alt="`${title}書封`"
      loading="lazy"
      @error="imageFailed = true"
    />
    <div v-else class="book-cover__fallback" role="img" :aria-label="`${title}文字書封`">
      <span class="book-cover__title">{{ title }}</span>
      <span v-if="author" class="book-cover__author">{{ author }}</span>
    </div>
  </div>
</template>
