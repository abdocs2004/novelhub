import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { authApi } from '../api/index.js'
import { TOKEN_KEY, USER_KEY } from '../api/client.js'

const AuthContext = createContext(null)

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY))
  } catch {
    return null
  }
}

/**
 * Keeps "who is logged in" in one place and shares it with every component (via useAuth()).
 * The JWT is kept in localStorage so the session survives a page refresh.
 * Trade-off: localStorage is readable by any JavaScript on the page (XSS). An httpOnly cookie is safer,
 * but needs CSRF protection. For this learning project localStorage keeps the flow easy to see.
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser)

  const saveSession = useCallback((auth) => {
    const { token, ...profile } = auth
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(profile))
    setUser(profile)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    setUser(null)
  }, [])

  const login = useCallback(async (credentials) => saveSession(await authApi.login(credentials)), [saveSession])
  const register = useCallback(async (details) => saveSession(await authApi.register(details)), [saveSession])

  // The Axios interceptor fires this when the server answers 401 (expired token).
  useEffect(() => {
    window.addEventListener('auth:expired', logout)
    return () => window.removeEventListener('auth:expired', logout)
  }, [logout])

  // On first load, ask the server who we are: refreshes the role and detects an expired token.
  useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) return
    authApi
      .me()
      .then((me) => {
        const profile = { id: me.id, name: me.name, email: me.email, role: me.role }
        localStorage.setItem(USER_KEY, JSON.stringify(profile))
        setUser(profile)
      })
      .catch(() => {})
  }, [])

  const value = useMemo(
    () => ({ user, isAdmin: user?.role === 'ADMIN', login, register, logout }),
    [user, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext)
}
