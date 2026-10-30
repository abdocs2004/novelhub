export function Loading({ label = 'Loading...' }) {
  return <p className="py-10 text-center text-sm text-muted">{label}</p>
}

export function ErrorMessage({ children }) {
  if (!children) return null
  return (
    <p role="alert" className="rounded-md border border-danger/30 bg-danger/5 px-3 py-2 text-sm text-danger">
      {children}
    </p>
  )
}

export function Notice({ children }) {
  if (!children) return null
  return (
    <p role="status" className="rounded-md border border-brand/30 bg-brand/5 px-3 py-2 text-sm text-brand-dark">
      {children}
    </p>
  )
}

/** Label + input wrapper that also shows the validation error coming from the backend. */
export function Field({ label, error, children }) {
  return (
    <label className="block">
      <span className="mb-1 block text-sm font-medium">{label}</span>
      {children}
      {error && <span className="mt-1 block text-xs text-danger">{error}</span>}
    </label>
  )
}

export function PageTitle({ children, action }) {
  return (
    <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
      <h1 className="font-serif text-3xl font-semibold">{children}</h1>
      {action}
    </div>
  )
}
