import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { errorMessage } from '../api/client.js'
import { chapterApi, novelApi, purchaseApi } from '../api/index.js'
import { ErrorMessage, Loading } from '../components/ui.jsx'
import { useAsync } from '../hooks/useAsync.js'
import { formatPrice } from '../format.js'

export default function ChapterReading() {
  const { novelId, chapterId } = useParams()
  const { data: chapter, loading, error, errorCode, reload } = useAsync(() => chapterApi.read(chapterId), [chapterId])

  if (loading) return <Loading label="Opening chapter..." />

  // The backend answered 403 CHAPTER_LOCKED: it did not send any text. We only show a purchase prompt.
  if (errorCode === 'CHAPTER_LOCKED') return <LockedChapter novelId={novelId} chapterId={chapterId} onPurchased={reload} />
  if (error) return <ErrorMessage>{error}</ErrorMessage>

  // Paragraphs are separated by blank lines in the stored text.
  const paragraphs = chapter.content.split(/\n{2,}/)

  return (
    <article className="mx-auto max-w-[38rem]">
      <Link to={`/novels/${chapter.novelId}`} className="text-sm text-brand hover:underline">
        {chapter.novelTitle}
      </Link>
      <p className="mt-6 text-sm text-muted">Chapter {chapter.chapterNumber}</p>
      <h1 className="mb-8 font-serif text-3xl font-semibold">{chapter.title}</h1>

      <div className="space-y-5 font-serif text-lg leading-[1.85]">
        {paragraphs.map((text, i) => (
          <p key={i} className="whitespace-pre-line">
            {text}
          </p>
        ))}
      </div>

      <nav className="mt-12 flex items-center justify-between border-t border-line pt-5">
        {chapter.previousChapterId ? (
          <Link to={`/novels/${chapter.novelId}/chapters/${chapter.previousChapterId}`} className="btn btn-outline">
            Previous chapter
          </Link>
        ) : (
          <span />
        )}
        {chapter.nextChapterId ? (
          <Link to={`/novels/${chapter.novelId}/chapters/${chapter.nextChapterId}`} className="btn btn-outline">
            Next chapter
          </Link>
        ) : (
          <span />
        )}
      </nav>
    </article>
  )
}

function LockedChapter({ novelId, chapterId, onPurchased }) {
  // Price and number are public information (they are on the novel page), so we can show them here.
  const { data, loading } = useAsync(() => novelApi.get(novelId), [novelId])
  const [paying, setPaying] = useState(false)
  const [error, setError] = useState('')

  const chapter = data?.chapters.find((c) => String(c.id) === String(chapterId))

  const buy = async () => {
    setPaying(true)
    setError('')
    try {
      await purchaseApi.buy(chapterId)
      onPurchased() // ask the backend for the chapter again: now it will answer 200 with the text
    } catch (err) {
      setError(errorMessage(err))
      setPaying(false)
    }
  }

  return (
    <div className="panel mx-auto max-w-md p-6 text-center">
      <h1 className="font-serif text-2xl font-semibold">This chapter is locked</h1>
      {loading ? (
        <Loading />
      ) : (
        <p className="mt-2 text-muted">
          {chapter ? `Chapter ${chapter.chapterNumber}: ${chapter.title}` : 'Buy this chapter to keep reading.'}
        </p>
      )}
      <div className="mt-4">
        <ErrorMessage>{error}</ErrorMessage>
      </div>
      <div className="mt-5 flex flex-wrap justify-center gap-3">
        <button className="btn btn-primary" onClick={buy} disabled={paying}>
          {paying ? 'Paying...' : `Buy ${chapter ? formatPrice(chapter.price) : 'chapter'}`}
        </button>
        <Link to={`/novels/${novelId}`} className="btn btn-outline">
          Back to novel
        </Link>
      </div>
    </div>
  )
}
