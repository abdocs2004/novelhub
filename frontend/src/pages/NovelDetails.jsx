import { useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { errorMessage } from '../api/client.js'
import { novelApi, purchaseApi } from '../api/index.js'
import Cover from '../components/Cover.jsx'
import { ErrorMessage, Loading, Notice } from '../components/ui.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { useAsync } from '../hooks/useAsync.js'
import { formatPrice } from '../format.js'

export default function NovelDetails() {
  const { novelId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  // Re-fetch when the user logs in/out: the backend returns a different "unlocked" flag per user.
  const { data, loading, error, reload } = useAsync(() => novelApi.get(novelId), [novelId, user?.id])
  const [buyingId, setBuyingId] = useState(null)
  const [message, setMessage] = useState('')
  const [buyError, setBuyError] = useState('')

  if (loading && !data) return <Loading />
  if (error) return <ErrorMessage>{error}</ErrorMessage>

  const { novel, chapters } = data

  const buy = async (chapter) => {
    if (!user) {
      navigate('/login', { state: { from: location.pathname } })
      return
    }
    setBuyingId(chapter.id)
    setMessage('')
    setBuyError('')
    try {
      await purchaseApi.buy(chapter.id) // mock payment on the server
      setMessage(`Chapter ${chapter.chapterNumber} purchased. You can read it now.`)
      reload()
    } catch (err) {
      setBuyError(errorMessage(err))
    } finally {
      setBuyingId(null)
    }
  }

  return (
    <div className="grid gap-8 md:grid-cols-[13rem_1fr]">
      <div className="mx-auto w-40 md:w-full">
        <Cover novel={novel} />
      </div>

      <div>
        <h1 className="font-serif text-3xl font-semibold">{novel.title}</h1>
        <p className="mt-1 text-muted">by {novel.author}</p>
        {novel.description && <p className="mt-4 max-w-prose leading-relaxed">{novel.description}</p>}

        <h2 className="mt-8 mb-3 font-serif text-xl font-semibold">Chapters</h2>
        <div className="mb-3 space-y-2">
          <Notice>{message}</Notice>
          <ErrorMessage>{buyError}</ErrorMessage>
        </div>

        {chapters.length === 0 && <p className="text-muted">No chapters yet.</p>}

        <ul className="panel divide-y divide-line">
          {chapters.map((chapter) => (
            <li key={chapter.id} className="flex items-center justify-between gap-3 px-4 py-3">
              <div className="min-w-0">
                <p className="truncate font-medium">
                  <span className="text-muted">{chapter.chapterNumber}.</span> {chapter.title}
                </p>
                <p className={`text-xs ${chapter.free ? 'text-brand' : 'text-ochre'}`}>{formatPrice(chapter.price)}</p>
              </div>

              {chapter.unlocked ? (
                <Link
                  to={`/novels/${novel.id}/chapters/${chapter.id}`}
                  className="btn btn-outline btn-sm"
                  onClick={(e) => {
                    if (!user) {
                      e.preventDefault()
                      navigate('/login', { state: { from: `/novels/${novel.id}/chapters/${chapter.id}` } })
                    }
                  }}
                >
                  Read
                </Link>
              ) : (
                <button className="btn btn-primary btn-sm" disabled={buyingId === chapter.id} onClick={() => buy(chapter)}>
                  {buyingId === chapter.id ? 'Paying...' : `Buy ${formatPrice(chapter.price)}`}
                </button>
              )}
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}
