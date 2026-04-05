import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { saveRecord, fetchMasters } from '../firestoreService'
import type { ExaminationItem, ItemMaster } from '../types'

interface Props { uid: string }

const emptyItem = (): ExaminationItem => ({
  itemName: '', value: '', unit: '',
  referenceMin: null, referenceMax: null, isAbnormal: false,
})

export default function RecordForm({ uid }: Props) {
  const navigate = useNavigate()
  const today = new Date().toISOString().slice(0, 10)

  const [date, setDate] = useState(today)
  const [facility, setFacility] = useState('')
  const [items, setItems] = useState<ExaminationItem[]>([emptyItem()])
  const [masters, setMasters] = useState<ItemMaster[]>([])
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    fetchMasters(uid).then(setMasters)
  }, [uid])

  const updateItem = (index: number, field: keyof ExaminationItem, value: string) => {
    setItems(prev => {
      const next = [...prev]
      const item = { ...next[index] }

      if (field === 'itemName') {
        // 項目名選択時にマスターから基準値を自動補完
        item.itemName = value
        const master = masters.find(m => m.itemName === value)
        if (master) {
          item.unit = master.unit
          item.referenceMin = master.referenceMin
          item.referenceMax = master.referenceMax
        }
      } else if (field === 'referenceMin' || field === 'referenceMax') {
        (item as Record<string, unknown>)[field] = value === '' ? null : Number(value)
      } else {
        (item as Record<string, unknown>)[field] = value
      }

      // isAbnormal を再計算
      const num = parseFloat(item.value)
      item.isAbnormal = !isNaN(num) && (
        (item.referenceMin != null && num < item.referenceMin) ||
        (item.referenceMax != null && num > item.referenceMax)
      )

      next[index] = item
      return next
    })
  }

  const addItem = () => setItems(prev => [...prev, emptyItem()])
  const removeItem = (index: number) =>
    setItems(prev => prev.filter((_, i) => i !== index))

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!date) return
    setSaving(true)
    try {
      const validItems = items.filter(i => i.itemName.trim())
      await saveRecord(uid, { date, facility, items: validItems })
      navigate('/')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <h2>新規記録追加</h2>
      <form onSubmit={handleSubmit} className="record-form">
        <div className="form-row">
          <label>受診日 <span className="required">*</span></label>
          <input
            type="date"
            value={date}
            onChange={e => setDate(e.target.value)}
            required
          />
        </div>

        <div className="form-row">
          <label>施設名</label>
          <input
            type="text"
            value={facility}
            onChange={e => setFacility(e.target.value)}
            placeholder="例: ○○クリニック"
          />
        </div>

        <div className="items-section">
          <div className="items-header">
            <span>検査項目</span>
            <button type="button" className="btn-add-item" onClick={addItem}>
              ＋ 項目追加
            </button>
          </div>

          {items.map((item, i) => (
            <div key={i} className={`item-row ${item.isAbnormal ? 'item-abnormal' : ''}`}>
              <div className="item-fields">
                <div className="item-field-name">
                  {masters.length > 0 ? (
                    <select
                      value={item.itemName}
                      onChange={e => updateItem(i, 'itemName', e.target.value)}
                    >
                      <option value="">項目を選択</option>
                      {masters.map(m => (
                        <option key={m.itemName} value={m.itemName}>{m.itemName}</option>
                      ))}
                      <option value="__custom__">直接入力</option>
                    </select>
                  ) : (
                    <input
                      type="text"
                      value={item.itemName}
                      onChange={e => updateItem(i, 'itemName', e.target.value)}
                      placeholder="項目名"
                    />
                  )}
                  {item.itemName === '__custom__' && (
                    <input
                      type="text"
                      placeholder="項目名を入力"
                      onChange={e => updateItem(i, 'itemName', e.target.value)}
                      className="custom-name"
                    />
                  )}
                </div>
                <input
                  type="text"
                  value={item.value}
                  onChange={e => updateItem(i, 'value', e.target.value)}
                  placeholder="値"
                  className="item-field-value"
                />
                <input
                  type="text"
                  value={item.unit}
                  onChange={e => updateItem(i, 'unit', e.target.value)}
                  placeholder="単位"
                  className="item-field-unit"
                />
              </div>
              <button
                type="button"
                className="btn-remove"
                onClick={() => removeItem(i)}
                disabled={items.length === 1}
              >
                ✕
              </button>
            </div>
          ))}
        </div>

        <div className="form-actions">
          <button type="button" className="btn-secondary" onClick={() => navigate('/')}>
            キャンセル
          </button>
          <button type="submit" className="btn-primary" disabled={saving}>
            {saving ? '保存中...' : '保存'}
          </button>
        </div>
      </form>
    </div>
  )
}
