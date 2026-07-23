import { describe, expect, it } from 'vitest'

import {
  validatePassword,
  validatePhoneNumber,
  validateUserName,
} from './validation'

describe('validation', () => {
  it('accepts a valid Taiwan mobile number', () => {
    expect(validatePhoneNumber('0912345678')).toBe('')
  })

  it('rejects an invalid mobile number', () => {
    expect(validatePhoneNumber('0212345678')).toBeTruthy()
  })

  it('enforces the password length range', () => {
    expect(validatePassword('short')).toBeTruthy()
    expect(validatePassword('Interview123!')).toBe('')
    expect(validatePassword('a'.repeat(20))).toBe('')
    expect(validatePassword('a'.repeat(21))).toBeTruthy()
  })

  it('rejects blank names and accepts a normal name', () => {
    expect(validateUserName('   ')).toBeTruthy()
    expect(validateUserName('王小明')).toBe('')
    expect(validateUserName('John Smith')).toBe('')
    expect(validateUserName('a'.repeat(30))).toBe('')
    expect(validateUserName('a'.repeat(31))).toBeTruthy()
  })

  it('rejects numbers and special characters in names', () => {
    expect(validateUserName('王小明123')).toBeTruthy()
    expect(validateUserName('John@Smith')).toBeTruthy()
    expect(validateUserName("O'Connor")).toBeTruthy()
  })
})
