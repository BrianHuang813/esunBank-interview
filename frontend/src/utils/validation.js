export function validatePhoneNumber(value) {
  if (!value) {
    return '請輸入手機號碼'
  }
  return /^09\d{8}$/.test(value) ? '' : '請輸入 09 開頭的 10 碼手機號碼'
}

export function validatePassword(value) {
  if (!value) {
    return '請輸入密碼'
  }
  return value.length >= 8 && value.length <= 72 ? '' : '密碼長度需為 8 至 72 個字元'
}

export function validateUserName(value) {
  if (!value.trim()) {
    return '請輸入姓名'
  }
  return value.trim().length <= 100 ? '' : '姓名不可超過 100 個字元'
}
