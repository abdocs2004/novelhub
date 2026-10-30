import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { errorMessage, fieldErrors } from '../api/client.js'
import { ErrorMessage, Field, PageTitle } from '../components/ui.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [errors, setErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)

  const update = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    setErrors({})
    try {
      await login(form)
      navigate(location.state?.from ?? '/', { replace: true }) // go back to the page that required login
    } catch (err) {
      setError(errorMessage(err))
      setErrors(fieldErrors(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-sm">
      <PageTitle>Log in</PageTitle>
      <form onSubmit={handleSubmit} className="panel space-y-4 p-5">
        <ErrorMessage>{error}</ErrorMessage>
        <Field label="Email" error={errors.email}>
          <input className="input" type="email" autoComplete="email" value={form.email} onChange={update('email')} required />
        </Field>
        <Field label="Password" error={errors.password}>
          <input
            className="input"
            type="password"
            autoComplete="current-password"
            value={form.password}
            onChange={update('password')}
            required
          />
        </Field>
        <button className="btn btn-primary w-full" disabled={submitting}>
          {submitting ? 'Logging in...' : 'Log in'}
        </button>
      </form>
      <p className="mt-4 text-center text-sm text-muted">
        New here?{' '}
        <Link to="/register" className="text-brand underline">
          Create an account
        </Link>
      </p>
    </div>
  )
}
