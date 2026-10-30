import { Link } from 'react-router-dom'
import { novelApi } from '../api/index.js'
import Cover from '../components/Cover.jsx'
import { ErrorMessage, Loading, PageTitle } from '../components/ui.jsx'
import { useAsync } from '../hooks/useAsync.js'

export default function Home() {
  const { data: novels, loading, error } = useAsync(novelApi.list)

  if (loading) return <Loading label="Loading novels..." />

  return (
    <>
      <PageTitle>Novels</PageTitle>
      <ErrorMessage>{error}</ErrorMessage>

      {novels?.length === 0 && <p className="text-muted">No novels yet. An admin can add the first one.</p>}

      <ul className="grid grid-cols-2 gap-x-5 gap-y-8 sm:grid-cols-3 md:grid-cols-4">
        {novels?.map((novel) => (
          <li key={novel.id}>
            <Link to={`/novels/${novel.id}`} className="group block">
              <Cover novel={novel} />
              <h2 className="mt-3 font-serif text-base leading-snug font-semibold group-hover:text-brand">{novel.title}</h2>
              <p className="text-sm text-muted">{novel.author}</p>
              <p className="text-xs text-muted">
                {novel.chapterCount} {novel.chapterCount === 1 ? 'chapter' : 'chapters'}
              </p>
            </Link>
          </li>
        ))}
      </ul>
    </>
  )
}
