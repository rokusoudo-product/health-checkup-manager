import { useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route, Link, NavLink, Navigate } from 'react-router-dom'
import { onAuthStateChanged, signInWithPopup, signOut } from 'firebase/auth'
import type { User } from 'firebase/auth'
import { auth, googleProvider } from './firebase'
import RecordList from './pages/RecordList'
import RecordDetail from './pages/RecordDetail'
import RecordForm from './pages/RecordForm'
import ItemMasters from './pages/ItemMasters'
import './App.css'

function LoginPage() {
  const [error, setError] = useState<string | null>(null)
  const handleSignIn = async () => {
    setError(null)
    try {
      await signInWithPopup(auth, googleProvider)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'ログインに失敗しました')
    }
  }
  return (
    <div className="page-center">
      <div className="card">
        <h1>健診ノート</h1>
        <p className="lead">健康診断結果を記録・管理するアプリです</p>
        {error && <p className="error">{error}</p>}
        <button className="btn-google" onClick={handleSignIn}>
          <img
            src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg"
            alt="Google" width="20" height="20"
          />
          Googleでログイン
        </button>
      </div>
    </div>
  )
}

function Layout({ user, children }: { user: User; children: React.ReactNode }) {
  return (
    <div className="layout">
      <nav className="navbar">
        <Link to="/" className="nav-brand">健診ノート</Link>
        <div className="nav-links">
          <NavLink to="/" end className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            記録一覧
          </NavLink>
          <NavLink to="/masters" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            項目マスター
          </NavLink>
        </div>
        <div className="nav-user">
          {user.photoURL && <img src={user.photoURL} alt="" className="nav-avatar" />}
          <span className="nav-name">{user.displayName}</span>
          <button className="btn-logout" onClick={() => signOut(auth)}>ログアウト</button>
        </div>
      </nav>
      <main className="main-content">
        {children}
      </main>
    </div>
  )
}

export default function App() {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (u) => {
      setUser(u)
      setLoading(false)
    })
    return unsubscribe
  }, [])

  if (loading) return <div className="page-center"><p>読み込み中...</p></div>
  if (!user) return <LoginPage />

  return (
    <BrowserRouter>
      <Layout user={user}>
        <Routes>
          <Route path="/" element={<RecordList uid={user.uid} />} />
          <Route path="/records/new" element={<RecordForm uid={user.uid} />} />
          <Route path="/records/:id" element={<RecordDetail uid={user.uid} />} />
          <Route path="/masters" element={<ItemMasters uid={user.uid} />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  )
}
