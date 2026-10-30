import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const linkClass = ({ isActive }) =>
  `text-sm ${isActive ? 'font-semibold text-brand' : 'text-muted hover:text-ink'}`

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="border-b border-line bg-white">
      <div className="mx-auto flex max-w-5xl flex-wrap items-center gap-x-6 gap-y-2 px-4 py-3">
        <Link to="/" className="font-serif text-xl font-semibold text-brand">
          NovelHub
        </Link>

        <nav className="flex flex-1 flex-wrap items-center gap-x-5 gap-y-1">
          <NavLink to="/" end className={linkClass}>
            Home
          </NavLink>
          {user && (
            <NavLink to="/library" className={linkClass}>
              My Library
            </NavLink>
          )}
          {isAdmin && (
            <NavLink to="/admin" className={linkClass}>
              Admin
            </NavLink>
          )}
        </nav>

        <div className="flex items-center gap-3">
          {user ? (
            <>
              <span className="text-sm text-muted">{user.name}</span>
              <button onClick={handleLogout} className="btn btn-outline btn-sm">
                Log out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-outline btn-sm">
                Log in
              </Link>
              <Link to="/register" className="btn btn-primary btn-sm">
                Create account
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
