import { api } from './api'

export async function login(payload) {
  const response = await api.post('/auth/login', payload, { skipSessionExpiry: true })
  return response.data.data
}

export async function register(payload) {
  const response = await api.post('/auth/register', payload, { skipSessionExpiry: true })
  return response.data.data
}

export async function getBooks({ keyword = '', page = 0, size = 20 } = {}) {
  const response = await api.get('/books', {
    params: { keyword, page, size },
  })
  return response.data.data
}

export async function getBook(isbn) {
  const response = await api.get(`/books/${isbn}`)
  return response.data.data
}

export async function borrowInventory(inventoryId) {
  const response = await api.post('/borrowings', { inventoryId })
  return response.data.data
}

export async function getMyBorrowings({ status = 'ALL', page = 0, size = 20 } = {}) {
  const response = await api.get('/borrowings/me', {
    params: { status, page, size },
  })
  return response.data.data
}

export async function returnBorrowing(borrowingId) {
  const response = await api.post(`/borrowings/${borrowingId}/return`)
  return response.data.data
}
