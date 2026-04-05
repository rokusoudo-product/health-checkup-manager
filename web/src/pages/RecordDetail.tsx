import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { fetchRecord, deleteRecord } from '../firestoreService'
import type { ExaminationRecord } from '../types'

interface Props { uid: string }

export default function RecordDetail({ uid }: Props) {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [record, setRecord] = useState<ExaminationRecord | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    fetchRecord(uid, id)
      .then(setRecord)
      .finally(() => setLoading(false))
  }, [uid, id])

  const handleDelete = async () => {
    if (!id || !confirm('この記録を削除しますか？')) return
    await deleteRecord(uid, id)
    navigate('/')
  }

  if (loading) return <p className="msg">読み込み中...</p>
  if (!record) return <p className="msg">記録が見つかりません。</p>

  return (
    <div>
      <div className="detail-header">
        <Link to="/" className="btn-back">← 一覧へ</Link>
        <button className="btn-danger" onClick={handleDelete}>削除</button>
      </div>

      <h2>{record.date}</h2>
      <p className="facility">{record.facility || '施設名未設定'}</p>

      <table className="items-table">
        <thead>
          <tr>
            <th>項目名</th>
            <th>値</th>
            <th>単位</th>
            <th>基準値</th>
          </tr>
        </thead>
        <tbody>
          {record.items.map((item, i) => (
            <tr key={i} className={item.isAbnormal ? 'row-abnormal' : ''}>
              <td>{item.itemName}</td>
              <td className="val">{item.value}</td>
              <td>{item.unit}</td>
              <td className="ref">
                {item.referenceMin != null || item.referenceMax != null
                  ? `${item.referenceMin ?? '—'} 〜 ${item.referenceMax ?? '—'}`
                  : '—'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
