import { Link } from 'react-router-dom'
import { purchaseApi } from '../api/index.js'
import { ErrorMessage, Loading, PageTitle } from '../components/ui.jsx'
import { useAsync } from '../hooks/useAsync.js'
import { formatDate, formatPrice } from '../format.js'

export default function MyLibrary() {
  const { data: purchases, loading, error } = useAsync(purchaseApi.mine)

  if (loading) return <Loading label="Loading your library..." />

  // The API returns a flat list; group it by novel for display.
  const byNovel = new Map()
  for (const purchase of purchases ?? []) {
    if (!byNovel.has(purchase.novelId)) byNovel.set(purchase.novelId, { title: purchase.novelTitle, items: [] })
    byNovel.get(purchase.novelId).items.push(purchase)
  }

  return (
    <>
      <PageTitle>My Library</PageTitle>
      <ErrorMessage>{error}</ErrorMessage>

      {purchases?.length === 0 && (
        <p className="text-muted">
          You have not bought any chapters yet.{' '}
          <Link to="/" className="text-brand underline">
            Browse novels
          </Link>
        </p>
      )}

      <div className="space-y-8">
        {[...byNovel.entries()].map(([novelId, group]) => (
          <section key={novelId}>
            <h2 className="mb-2 font-serif text-xl font-semibold">
              <Link to={`/novels/${novelId}`} className="hover:text-brand">
                {group.title}
              </Link>
            </h2>
            <ul className="panel divide-y divide-line">
              {group.items
                .sort((a, b) => a.chapterNumber - b.chapterNumber)
                .map((item) => (
                  <li key={item.id} className="flex items-center justify-between gap-3 px-4 py-3">
                    <div className="min-w-0">
                      <p className="truncate font-medium">
                        <span className="text-muted">{item.chapterNumber}.</span> {item.chapterTitle}
                      </p>
                      <p className="text-xs text-muted">
                        Bought {formatDate(item.purchasedAt)} for {formatPrice(item.price)}
                      </p>
                    </div>
                    <Link to={`/novels/${novelId}/chapters/${item.chapterId}`} className="btn btn-outline btn-sm">
                      Read
                    </Link>
                  </li>
                ))}
            </ul>
          </section>
        ))}
      </div>
    </>
  )
}
