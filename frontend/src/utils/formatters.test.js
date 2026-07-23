import { describe, expect, it } from 'vitest'

import {
  borrowingStatusLabel,
  formatDateTime,
  inventoryStatusLabel,
} from './formatters'

describe('formatters', () => {
  it('maps inventory statuses to readable labels', () => {
    expect(inventoryStatusLabel('AVAILABLE')).toBe('可借閱')
    expect(inventoryStatusLabel('BORROWED')).toBe('已借出')
    expect(inventoryStatusLabel('UNKNOWN')).toBe('狀態不明')
  })

  it('identifies active and returned records', () => {
    expect(borrowingStatusLabel({ returnTime: null })).toBe('借閱中')
    expect(borrowingStatusLabel({ returnTime: '2026-07-23T10:00:00+08:00' })).toBe('已歸還')
  })

  it('formats timestamps in the Taipei timezone', () => {
    expect(formatDateTime('2026-07-23T02:30:00Z')).toContain('10:30')
    expect(formatDateTime(null)).toBe('尚未歸還')
  })
})
