import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchRecords } from '../firestoreService'
import type { ExaminationRecord } from '../types'

interface Props { uid: string }

export default function RecordList({ uid }: Props) {
  const [records, setRecords] = useState<ExaminationRecord[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchRecords(uid)
      .then(setRecords)
      .finally(() => setLoading(false))
  }, [uid])

  if (loading) return <p className="msg">読み込み中...</p>

  return (
    <div>
      <div className="list-header">
        <h2>診断記録一覧</h2>
        <Link to="/records/new" className="btn-primary">＋ 新規追加</Link>
      </div>

      {records.length === 0 ? (
        <p className="msg">記録がありません。「＋ 新規追加」から登録してください。</p>
      ) : (
        <ul className="record-list">
          {records.map(r => {
            const abnormalCount = r.items.filter(i => i.isAbnormal).length
            return (
              <li key={r.id}>
                <Link to={`/records/${r.id}`} className="record-card">
                  <div className="record-date">{r.date}</div>
                  <div className="record-facility">{r.facility || '施設名未設定'}</div>
                  <div className="record-meta">
                    <span>{r.items.length} 項目</span>
                    {abnormalCount > 0 && (
                      <span className="badge-abnormal">{abnormalCount} 件基準値外</span>
                    )}
                  </div>
                </Link>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}
