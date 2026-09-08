import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchRecords } from '../firestoreService'
import type { ExaminationRecord } from '../types'
import { buildAbnormalEntries, formatReferenceRange } from '../lib/abnormal'

interface Props { uid: string }

/**
 * US-W05 / T-602: Web版 基準値外一覧画面。
 * 全記録のうち isAbnormal===true の項目のみを検査日の新しい順（項目単位のフラット表示）で一覧表示する。
 * 薬事法対応: 「基準値外である事実の表示」にとどめ、医療的判断・改善提案・受診勧告の文言は一切含めない
 * （docs/compliance.md §2-2 参照）。
 */
export default function AbnormalList({ uid }: Props) {
  const navigate = useNavigate()
  const [records, setRecords] = useState<ExaminationRecord[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchRecords(uid)
      .then(setRecords)
      .finally(() => setLoading(false))
  }, [uid])

  if (loading) return <p className="msg">読み込み中...</p>

  const entries = buildAbnormalEntries(records)

  const goToRecord = (recordId: string) => navigate(`/records/${recordId}`)

  return (
    <div>
      <h2>基準値外一覧</h2>
      <p className="note-small">
        これまでの記録のうち、基準値外だった項目を検査日の新しい順に表示します。
        基準値はご自身で登録した参照値との比較であり、医学的な診断ではありません。
      </p>

      {entries.length === 0 ? (
        <p className="msg">基準値外の項目はありません。</p>
      ) : (
        <table className="abnormal-table">
          <thead>
            <tr>
              <th>検査日</th>
              <th>施設名</th>
              <th>項目名</th>
              <th>値</th>
              <th>基準値</th>
            </tr>
          </thead>
          <tbody>
            {entries.map((e, i) => (
              <tr
                key={`${e.recordId}-${e.itemName}-${i}`}
                className="abnormal-row"
                role="link"
                tabIndex={0}
                onClick={() => goToRecord(e.recordId)}
                onKeyDown={ev => {
                  if (ev.key === 'Enter' || ev.key === ' ') {
                    ev.preventDefault()
                    goToRecord(e.recordId)
                  }
                }}
              >
                <td>{e.date}</td>
                <td>{e.facility || '施設名未設定'}</td>
                <td>{e.itemName}</td>
                <td className="val">{[e.value, e.unit].filter(Boolean).join(' ')}</td>
                <td className="ref">{formatReferenceRange(e.referenceMin, e.referenceMax)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
