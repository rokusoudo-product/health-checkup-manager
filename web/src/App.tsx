import { useEffect, useState } from 'react'
import { onAuthStateChanged, signInWithPopup, signOut } from 'firebase/auth'
import type { User } from 'firebase/auth'
import { auth, googleProvider } from './firebase'
import './App.css'

function App() {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (u) => {
      setUser(u)
      setLoading(false)
    })
    return unsubscribe
  }, [])

  const handleSignIn = async () => {
    setError(null)
    try {
      await signInWithPopup(auth, googleProvider)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'ログインに失敗しました')
    }
  }

  const handleSignOut = async () => {
    await signOut(auth)
  }

  if (loading) {
    return <div className="page-center"><p>読み込み中...</p></div>
  }

  return (
    <div className="page-center">
      <div className="card">
        <h1>健診ノート</h1>

        {user ? (
          <div className="user-section">
            {user.photoURL && (
              <img src={user.photoURL} alt="avatar" className="avatar" />
            )}
            <p className="welcome">ようこそ、<strong>{user.displayName}</strong> さん</p>
            <p className="email">{user.email}</p>
            <p className="note">記録機能は近日実装予定です</p>
            <button className="btn-secondary" onClick={handleSignOut}>
              ログアウト
            </button>
          </div>
        ) : (
          <div className="login-section">
            <p className="lead">健康診断結果を記録・管理するアプリです</p>
            {error && <p className="error">{error}</p>}
            <button className="btn-google" onClick={handleSignIn}>
              <img
                src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg"
                alt="Google"
                width="20"
                height="20"
              />
              Googleでログイン
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

export default App
