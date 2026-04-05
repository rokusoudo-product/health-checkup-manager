import { useEffect, useState } from 'react'
import { fetchMasters, saveMaster, deleteMaster } from '../firestoreService'
import type { ItemMaster } from '../types'

interface Props { uid: string }

const emptyMaster = (): ItemMaster => ({
  itemName: '', unit: '', referenceMin: null, referenceMax: null,
})

export default function ItemMasters({ uid }: Props) {
  const [masters, setMasters] = useState<ItemMaster[]>([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState<ItemMaster>(emptyMaster())
  const [saving, setSaving] = useState(false)

  const load = () =>
    fetchMasters(uid).then(setMasters).finally(() => setLoading(false))

  useEffect(() => { load() }, [uid])

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.itemName.trim()) return
    setSaving(true)
    try {
      await saveMaster(uid, form)
      setForm(emptyMaster())
      await load()
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (itemName: string) => {
    if (!confirm(`「${itemName}」を削除しますか？`)) return
    await deleteMaster(uid, itemName)
    await load()
  }

  const handleEdit = (m: ItemMaster) => setForm({ ...m })

  if (loading) return <p className="msg">読み込み中...</p>

  return (
    <div>
      <h2>項目マスター管理</h2>
      <p className="note-small">検査項目の単位・基準値を登録すると、記録入力時に自動補完されます。</p>

      <form onSubmit={handleSave} className="master-form">
        <div className="master-form-row">
          <input
            type="text"
            value={form.itemName}
            onChange={e => setForm(f => ({ ...f, itemName: e.target.value }))}
            placeholder="項目名（例: 血圧（収縮期））"
            required
            className="master-input-name"
          />
          <input
            type="text"
            value={form.unit}
            onChange={e => setForm(f => ({ ...f, unit: e.target.value }))}
            placeholder="単位（例: mmHg）"
            className="master-input-unit"
          />
          <input
            type="number"
            value={form.referenceMin ?? ''}
            onChange={e => setForm(f => ({ ...f, referenceMin: e.target.value === '' ? null : Number(e.target.value) }))}
            placeholder="基準値（下限）"
            className="master-input-ref"
          />
          <input
            type="number"
            value={form.referenceMax ?? ''}
            onChange={e => setForm(f => ({ ...f, referenceMax: e.target.value === '' ? null : Number(e.target.value) }))}
            placeholder="基準値（上限）"
            className="master-input-ref"
          />
          <button type="submit" className="btn-primary" disabled={saving}>
            {saving ? '保存中...' : '登録'}
          </button>
        </div>
      </form>

      {masters.length === 0 ? (
        <p className="msg">項目マスターが登録されていません。</p>
      ) : (
        <table className="items-table">
          <thead>
            <tr>
              <th>項目名</th>
              <th>単位</th>
              <th>基準値（下限）</th>
              <th>基準値（上限）</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {masters.map(m => (
              <tr key={m.itemName}>
                <td>{m.itemName}</td>
                <td>{m.unit}</td>
                <td>{m.referenceMin ?? '—'}</td>
                <td>{m.referenceMax ?? '—'}</td>
                <td className="actions">
                  <button className="btn-text" onClick={() => handleEdit(m)}>編集</button>
                  <button className="btn-text danger" onClick={() => handleDelete(m.itemName)}>削除</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
