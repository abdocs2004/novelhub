import axios from 'axios'

export const TOKEN_KEY = 'novelhub_token'
export const USER_KEY = 'novelhub_user'

// One Axios instance for the whole app: same base URL and same JWT header everywhere.
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
})

// Before every request: attach "Authorization: Bearer <token>" if we have a token.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// After every response: if the server says 401 while we hold a token, the token is expired/invalid.
// We tell the AuthContext to log out (it listens to this event).
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && localStorage.getItem(TOKEN_KEY)) {
      window.dispatchEvent(new Event('auth:expired'))
    }
    return Promise.reject(error)
  },
)

/** Turns an Axios error into a message that is safe to show to the user. */
export function errorMessage(error) {
  return error.response?.data?.message ?? 'Cannot reach the server. Is the backend running?'
}

/** Field-level validation errors from Spring (@Valid), e.g. { email: "Email is not valid" }. */
export function fieldErrors(error) {
  return error.response?.data?.fieldErrors ?? {}
}

/** The machine-readable code of an API error, e.g. "CHAPTER_LOCKED". */
export function errorCode(error) {
  return error.response?.data?.code
}
