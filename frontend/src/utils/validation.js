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
  return value.length >= 8 && value.length <= 20 ? '' : '密碼長度需為 8 至 20 個字元'
}

export function validateUserName(value) {
  const trimmedValue = value.trim()
  if (!trimmedValue) {
    return '請輸入姓名'
  }
  if (trimmedValue.length > 30) {
    return '姓名不可超過 30 個字元'
  }
  return /^[\p{Script=Han}A-Za-z ]+$/u.test(trimmedValue)
    ? ''
    : '姓名只能包含中文、英文字母與空格'
}
