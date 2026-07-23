const dateTimeFormatter = new Intl.DateTimeFormat('zh-TW', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
  timeZone: 'Asia/Taipei',
})

export function formatDateTime(value) {
  if (!value) {
    return '尚未歸還'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '時間資料有誤'
  }

  return dateTimeFormatter.format(date)
}

export function inventoryStatusLabel(status) {
  const labels = {
    AVAILABLE: '可借閱',
    BORROWED: '已借出',
    PROCESSING: '整理中',
    LOST: '遺失',
    DAMAGED: '損壞',
    DISCARDED: '已淘汰',
  }

  return labels[status] ?? '狀態不明'
}

export function borrowingStatusLabel(record) {
  return record.returnTime ? '已歸還' : '借閱中'
}
