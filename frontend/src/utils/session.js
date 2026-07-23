const SESSION_KEY = 'esun-library-session'

export function readSession() {
  try {
    const raw = window.sessionStorage.getItem(SESSION_KEY)
    if (!raw) {
      return null
    }

    const parsed = JSON.parse(raw)
    if (!parsed?.accessToken || !parsed?.user?.id || !parsed?.user?.userName) {
      clearSession()
      return null
    }

    return parsed
  } catch {
    clearSession()
    return null
  }
}

export function writeSession(session) {
  window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(session))
}

export function clearSession() {
  window.sessionStorage.removeItem(SESSION_KEY)
}

export function hasSession() {
  return Boolean(readSession()?.accessToken)
}

export function getAccessToken() {
  return readSession()?.accessToken ?? null
}
