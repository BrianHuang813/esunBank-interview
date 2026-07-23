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
const imageLoaded = ref(false)
const imageUrl = computed(
  () => `https://covers.openlibrary.org/b/isbn/${props.isbn}-L.jpg?default=false`,
)

function handleLoad(event) {
  if (event.currentTarget.naturalWidth <= 1) {
    imageFailed.value = true
    return
  }
  imageLoaded.value = true
}

watch(
  () => props.isbn,
  () => {
    imageFailed.value = false
    imageLoaded.value = false
  },
)
</script>

<template>
  <div
    class="book-cover"
    :class="`book-cover--${size}`"
    role="img"
    :aria-label="`${title}書封`"
  >
    <div class="book-cover__fallback" aria-hidden="true">
      <span class="book-cover__title">{{ title }}</span>
      <span v-if="author" class="book-cover__author">{{ author }}</span>
    </div>
    <img
      v-if="!imageFailed"
      :src="imageUrl"
      alt=""
      loading="lazy"
      :class="{ 'book-cover__image--loaded': imageLoaded }"
      @load="handleLoad"
      @error="imageFailed = true"
    />
  </div>
</template>
