import { Link, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import NovelDetails from './pages/NovelDetails.jsx'
import ChapterReading from './pages/ChapterReading.jsx'
import MyLibrary from './pages/MyLibrary.jsx'
import AdminDashboard from './pages/admin/AdminDashboard.jsx'
import ManageNovels from './pages/admin/ManageNovels.jsx'
import ManageChapters from './pages/admin/ManageChapters.jsx'

function NotFound() {
  return (
    <p className="py-16 text-center text-muted">
      This page does not exist.{' '}
      <Link to="/" className="text-brand underline">
        Back to novels
      </Link>
    </p>
  )
}

export default function App() {
  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-5xl px-4 py-8">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/novels/:novelId" element={<NovelDetails />} />

          {/* Logged-in users only */}
          <Route element={<ProtectedRoute />}>
            <Route path="/novels/:novelId/chapters/:chapterId" element={<ChapterReading />} />
            <Route path="/library" element={<MyLibrary />} />
          </Route>

          {/* Admins only */}
          <Route element={<ProtectedRoute adminOnly />}>
            <Route path="/admin" element={<AdminDashboard />} />
            <Route path="/admin/novels" element={<ManageNovels />} />
            <Route path="/admin/novels/:novelId/chapters" element={<ManageChapters />} />
          </Route>

          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
    </>
  )
}
