import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { errorMessage, fieldErrors } from '../api/client.js'
import { ErrorMessage, Field, PageTitle } from '../components/ui.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '' })
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
      await register(form) // the backend logs us in right away and returns a token
      navigate('/', { replace: true })
    } catch (err) {
      setError(errorMessage(err))
      setErrors(fieldErrors(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-sm">
      <PageTitle>Create account</PageTitle>
      <form onSubmit={handleSubmit} className="panel space-y-4 p-5">
        <ErrorMessage>{error}</ErrorMessage>
        <Field label="Name" error={errors.name}>
          <input className="input" autoComplete="name" value={form.name} onChange={update('name')} required />
        </Field>
        <Field label="Email" error={errors.email}>
          <input className="input" type="email" autoComplete="email" value={form.email} onChange={update('email')} required />
        </Field>
        <Field label="Password (6 characters or more)" error={errors.password}>
          <input
            className="input"
            type="password"
            autoComplete="new-password"
            value={form.password}
            onChange={update('password')}
            minLength={6}
            required
          />
        </Field>
        <button className="btn btn-primary w-full" disabled={submitting}>
          {submitting ? 'Creating account...' : 'Create account'}
        </button>
      </form>
      <p className="mt-4 text-center text-sm text-muted">
        Already have an account?{' '}
        <Link to="/login" className="text-brand underline">
          Log in
        </Link>
      </p>
    </div>
  )
}
