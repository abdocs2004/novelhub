import { useState } from 'react'
import { Link } from 'react-router-dom'
import { adminApi } from '../../api/index.js'
import { ErrorMessage, Loading, PageTitle } from '../../components/ui.jsx'
import { useAsync } from '../../hooks/useAsync.js'
import { formatDate, formatPrice } from '../../format.js'

export default function AdminDashboard() {
  const users = useAsync(adminApi.users)
  const purchases = useAsync(adminApi.purchases)
  const [tab, setTab] = useState('purchases')

  if (users.loading || purchases.loading) return <Loading />

  const successful = (purchases.data ?? []).filter((p) => p.status === 'SUCCESS')
  const revenue = successful.reduce((sum, p) => sum + Number(p.price), 0)

  return (
    <>
      <PageTitle
        action={
          <Link to="/admin/novels" className="btn btn-primary">
            Manage novels
          </Link>
        }
      >
        Admin dashboard
      </PageTitle>
      <ErrorMessage>{users.error || purchases.error}</ErrorMessage>

      <dl className="mb-8 grid gap-4 sm:grid-cols-3">
        <Stat label="Users" value={users.data?.length ?? 0} />
        <Stat label="Chapters sold" value={successful.length} />
        <Stat label="Revenue (mock)" value={formatPrice(revenue)} />
      </dl>

      <div className="mb-3 flex gap-2" role="tablist">
        {['purchases', 'users'].map((name) => (
          <button
            key={name}
            role="tab"
            aria-selected={tab === name}
            onClick={() => setTab(name)}
            className={`btn btn-sm ${tab === name ? 'btn-primary' : 'btn-outline'}`}
          >
            {name === 'purchases' ? 'Purchases' : 'Users'}
          </button>
        ))}
      </div>

      <div className="panel overflow-x-auto">
        {tab === 'purchases' ? (
          <table className="w-full text-left text-sm">
            <thead className="border-b border-line text-muted">
              <tr>
                <th className="px-4 py-2 font-medium">Date</th>
                <th className="px-4 py-2 font-medium">User</th>
                <th className="px-4 py-2 font-medium">Chapter</th>
                <th className="px-4 py-2 font-medium">Price</th>
                <th className="px-4 py-2 font-medium">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-line">
              {purchases.data?.map((p) => (
                <tr key={p.id}>
                  <td className="px-4 py-2 whitespace-nowrap">{formatDate(p.purchasedAt)}</td>
                  <td className="px-4 py-2">
                    {p.userName}
                    <span className="block text-xs text-muted">{p.userEmail}</span>
                  </td>
                  <td className="px-4 py-2">
                    {p.novelTitle}
                    <span className="block text-xs text-muted">
                      {p.chapterNumber}. {p.chapterTitle}
                    </span>
                  </td>
                  <td className="px-4 py-2">{formatPrice(p.price)}</td>
                  <td className="px-4 py-2">{p.status}</td>
                </tr>
              ))}
              {purchases.data?.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-4 py-6 text-center text-muted">
                    No purchases yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        ) : (
          <table className="w-full text-left text-sm">
            <thead className="border-b border-line text-muted">
              <tr>
                <th className="px-4 py-2 font-medium">Name</th>
                <th className="px-4 py-2 font-medium">Email</th>
                <th className="px-4 py-2 font-medium">Role</th>
                <th className="px-4 py-2 font-medium">Joined</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-line">
              {users.data?.map((u) => (
                <tr key={u.id}>
                  <td className="px-4 py-2">{u.name}</td>
                  <td className="px-4 py-2">{u.email}</td>
                  <td className="px-4 py-2">{u.role}</td>
                  <td className="px-4 py-2 whitespace-nowrap">{formatDate(u.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  )
}

function Stat({ label, value }) {
  return (
    <div className="panel px-4 py-3">
      <dt className="text-sm text-muted">{label}</dt>
      <dd className="font-serif text-2xl font-semibold">{value}</dd>
    </div>
  )
}
