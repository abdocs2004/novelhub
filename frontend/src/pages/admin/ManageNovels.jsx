import { useState } from 'react'
import { Link } from 'react-router-dom'
import { errorMessage, fieldErrors } from '../../api/client.js'
import { adminApi, novelApi } from '../../api/index.js'
import { ErrorMessage, Field, Loading, Notice, PageTitle } from '../../components/ui.jsx'
import { useAsync } from '../../hooks/useAsync.js'

const EMPTY = { title: '', author: '', description: '', coverImage: '' }

export default function ManageNovels() {
  const { data: novels, loading, error, reload } = useAsync(novelApi.list)
  const [editingId, setEditingId] = useState(null) // null = the form creates a new novel
  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [message, setMessage] = useState('')
  const [saving, setSaving] = useState(false)

  const update = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  const reset = () => {
    setEditingId(null)
    setForm(EMPTY)
    setErrors({})
    setFormError('')
  }

  const startEdit = (novel) => {
    setEditingId(novel.id)
    setForm({
      title: novel.title,
      author: novel.author,
      description: novel.description ?? '',
      coverImage: novel.coverImage ?? '',
    })
    setMessage('')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const submit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setErrors({})
    setFormError('')
    try {
      if (editingId) await adminApi.updateNovel(editingId, form)
      else await adminApi.createNovel(form)
      setMessage(editingId ? 'Novel updated.' : 'Novel added.')
      reset()
      reload()
    } catch (err) {
      setFormError(errorMessage(err))
      setErrors(fieldErrors(err))
    } finally {
      setSaving(false)
    }
  }

  const remove = async (novel) => {
    if (!window.confirm(`Delete "${novel.title}", all its chapters and their purchases? This cannot be undone.`)) return
    try {
      await adminApi.deleteNovel(novel.id)
      setMessage('Novel deleted.')
      if (editingId === novel.id) reset()
      reload()
    } catch (err) {
      setFormError(errorMessage(err))
    }
  }

  return (
    <>
      <PageTitle
        action={
          <Link to="/admin" className="btn btn-outline">
            Dashboard
          </Link>
        }
      >
        Manage novels
      </PageTitle>

      <form onSubmit={submit} className="panel mb-8 grid gap-4 p-5 md:grid-cols-2">
        <h2 className="font-serif text-xl font-semibold md:col-span-2">{editingId ? 'Edit novel' : 'Add a novel'}</h2>
        <div className="space-y-2 md:col-span-2">
          <ErrorMessage>{formError}</ErrorMessage>
          <Notice>{message}</Notice>
        </div>
        <Field label="Title" error={errors.title}>
          <input className="input" value={form.title} onChange={update('title')} required />
        </Field>
        <Field label="Author" error={errors.author}>
          <input className="input" value={form.author} onChange={update('author')} required />
        </Field>
        <div className="md:col-span-2">
          <Field label="Description" error={errors.description}>
            <textarea className="input min-h-24" value={form.description} onChange={update('description')} />
          </Field>
        </div>
        <div className="md:col-span-2">
          <Field label="Cover image URL (optional)" error={errors.coverImage}>
            <input className="input" type="url" placeholder="https://..." value={form.coverImage} onChange={update('coverImage')} />
          </Field>
        </div>
        <div className="flex gap-3 md:col-span-2">
          <button className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving...' : editingId ? 'Save changes' : 'Add novel'}
          </button>
          {editingId && (
            <button type="button" className="btn btn-outline" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>

      {loading && !novels ? (
        <Loading />
      ) : (
        <>
          <ErrorMessage>{error}</ErrorMessage>
          <ul className="panel divide-y divide-line">
            {novels?.map((novel) => (
              <li key={novel.id} className="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
                <div className="min-w-0">
                  <p className="truncate font-medium">{novel.title}</p>
                  <p className="text-xs text-muted">
                    {novel.author} - {novel.chapterCount} chapters
                  </p>
                </div>
                <div className="flex gap-2">
                  <Link to={`/admin/novels/${novel.id}/chapters`} className="btn btn-outline btn-sm">
                    Chapters
                  </Link>
                  <button className="btn btn-outline btn-sm" onClick={() => startEdit(novel)}>
                    Edit
                  </button>
                  <button className="btn btn-danger btn-sm" onClick={() => remove(novel)}>
                    Delete
                  </button>
                </div>
              </li>
            ))}
            {novels?.length === 0 && <li className="px-4 py-6 text-center text-muted">No novels yet.</li>}
          </ul>
        </>
      )}
    </>
  )
}
