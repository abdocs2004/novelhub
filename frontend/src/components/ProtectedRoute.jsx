import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

/**
 * UX guard only! It hides pages from people who should not see them, but it is NOT security:
 * anyone can edit JavaScript in the browser. The real protection is in the backend (Spring Security).
 */
export default function ProtectedRoute({ adminOnly = false }) {
  const { user, isAdmin } = useAuth()
  const location = useLocation()

  if (!user) return <Navigate to="/login" state={{ from: location.pathname }} replace />
  if (adminOnly && !isAdmin) return <Navigate to="/" replace />
  return <Outlet />
}
