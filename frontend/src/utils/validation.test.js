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
    expect(validatePassword('a'.repeat(73))).toBeTruthy()
  })

  it('rejects blank names and accepts a normal name', () => {
    expect(validateUserName('   ')).toBeTruthy()
    expect(validateUserName('王小明')).toBe('')
  })
})
