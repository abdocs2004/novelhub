import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { errorMessage, fieldErrors } from '../../api/client.js'
import { adminApi, novelApi } from '../../api/index.js'
import { ErrorMessage, Field, Loading, Notice, PageTitle } from '../../components/ui.jsx'
import { useAsync } from '../../hooks/useAsync.js'
import { formatPrice } from '../../format.js'

export default function ManageChapters() {
  const { novelId } = useParams()
  const novel = useAsync(() => novelApi.get(novelId), [novelId])
  const chapters = useAsync(() => adminApi.chapters(novelId), [novelId])

  const [editingId, setEditingId] = useState(null)
  const [form, setForm] = useState({ chapterNumber: '', title: '', price: '0', content: '' })
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [message, setMessage] = useState('')
  const [saving, setSaving] = useState(false)

  const nextNumber = () => Math.max(0, ...(chapters.data ?? []).map((c) => c.chapterNumber)) + 1
  const update = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  const reset = () => {
    setEditingId(null)
    setForm({ chapterNumber: '', title: '', price: '0', content: '' })
    setErrors({})
    setFormError('')
  }

  const startEdit = (chapter) => {
    setEditingId(chapter.id)
    setForm({
      chapterNumber: String(chapter.chapterNumber),
      title: chapter.title,
      price: String(chapter.price),
      content: chapter.content,
    })
    setMessage('')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const submit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setErrors({})
    setFormError('')
    const body = {
      title: form.title,
      content: form.content,
      price: form.price === '' ? null : Number(form.price),
      chapterNumber: form.chapterNumber === '' ? nextNumber() : Number(form.chapterNumber),
    }
    try {
      if (editingId) await adminApi.updateChapter(editingId, body)
      else await adminApi.createChapter(novelId, body)
      setMessage(editingId ? 'Chapter updated.' : 'Chapter added.')
      reset()
      chapters.reload()
    } catch (err) {
      setFormError(errorMessage(err))
      setErrors(fieldErrors(err))
    } finally {
      setSaving(false)
    }
  }

  const remove = async (chapter) => {
    if (!window.confirm(`Delete chapter ${chapter.chapterNumber} and its purchases? This cannot be undone.`)) return
    try {
      await adminApi.deleteChapter(chapter.id)
      setMessage('Chapter deleted.')
      if (editingId === chapter.id) reset()
      chapters.reload()
    } catch (err) {
      setFormError(errorMessage(err))
    }
  }

  return (
    <>
      <PageTitle
        action={
          <Link to="/admin/novels" className="btn btn-outline">
            All novels
          </Link>
        }
      >
        {novel.data ? `Chapters of ${novel.data.novel.title}` : 'Chapters'}
      </PageTitle>
      <ErrorMessage>{novel.error}</ErrorMessage>

      <form onSubmit={submit} className="panel mb-8 grid gap-4 p-5 md:grid-cols-[8rem_1fr_8rem]">
        <h2 className="font-serif text-xl font-semibold md:col-span-3">{editingId ? 'Edit chapter' : 'Add a chapter'}</h2>
        <div className="space-y-2 md:col-span-3">
          <ErrorMessage>{formError}</ErrorMessage>
          <Notice>{message}</Notice>
        </div>
        <Field label="Number" error={errors.chapterNumber}>
          <input
            className="input"
            type="number"
            min="1"
            placeholder={String(nextNumber())}
            value={form.chapterNumber}
            onChange={update('chapterNumber')}
          />
        </Field>
        <Field label="Title" error={errors.title}>
          <input className="input" value={form.title} onChange={update('title')} required />
        </Field>
        <Field label="Price (0 = free)" error={errors.price}>
          <input className="input" type="number" min="0" step="0.01" value={form.price} onChange={update('price')} required />
        </Field>
        <div className="md:col-span-3">
          <Field label="Content (leave a blank line between paragraphs)" error={errors.content}>
            <textarea className="input min-h-56 font-serif" value={form.content} onChange={update('content')} required />
          </Field>
        </div>
        <div className="flex gap-3 md:col-span-3">
          <button className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving...' : editingId ? 'Save changes' : 'Add chapter'}
          </button>
          {editingId && (
            <button type="button" className="btn btn-outline" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>

      {chapters.loading && !chapters.data ? (
        <Loading />
      ) : (
        <>
          <ErrorMessage>{chapters.error}</ErrorMessage>
          <ul className="panel divide-y divide-line">
            {chapters.data?.map((chapter) => (
              <li key={chapter.id} className="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
                <div className="min-w-0">
                  <p className="truncate font-medium">
                    <span className="text-muted">{chapter.chapterNumber}.</span> {chapter.title}
                  </p>
                  <p className="text-xs text-muted">{formatPrice(chapter.price)}</p>
                </div>
                <div className="flex gap-2">
                  <button className="btn btn-outline btn-sm" onClick={() => startEdit(chapter)}>
                    Edit
                  </button>
                  <button className="btn btn-danger btn-sm" onClick={() => remove(chapter)}>
                    Delete
                  </button>
                </div>
              </li>
            ))}
            {chapters.data?.length === 0 && <li className="px-4 py-6 text-center text-muted">No chapters yet.</li>}
          </ul>
        </>
      )}
    </>
  )
}
