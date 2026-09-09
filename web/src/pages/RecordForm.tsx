import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { saveRecord, fetchMasters } from '../firestoreService'
import type { ItemMaster } from '../types'
import {
  CUSTOM_ITEM_OPTION_VALUE,
  createEmptyRow,
  selectItemName,
  setCustomItemName,
  toSavableItems,
  updateRowField,
  type FormItemRow,
} from '../lib/recordFormRow'

interface Props { uid: string }

export default function RecordForm({ uid }: Props) {
  const navigate = useNavigate()
  const today = new Date().toISOString().slice(0, 10)

  const [date, setDate] = useState(today)
  const [facility, setFacility] = useState('')
  // 行の識別には配列インデックスではなく安定した key を使う（Issue #51）。
  // インデックスで直接入力モードを管理すると、行を削除したときにモードが別の行にずれてしまうため、
  // 「直接入力モードかどうか」を行オブジェクト自身（FormItemRow.isCustom）に持たせている。
  const nextKey = useRef(1)
  const [rows, setRows] = useState<FormItemRow[]>([createEmptyRow('0')])
  const [masters, setMasters] = useState<ItemMaster[]>([])
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    fetchMasters(uid).then(setMasters)
  }, [uid])

  const updateRowAt = (index: number, updater: (row: FormItemRow) => FormItemRow) => {
    setRows(prev => prev.map((row, i) => (i === index ? updater(row) : row)))
  }

  const handleItemNameSelect = (index: number, value: string) =>
    updateRowAt(index, row => selectItemName(row, value, masters))

  const handleCustomNameChange = (index: number, value: string) =>
    updateRowAt(index, row => setCustomItemName(row, value))

  const handleFieldChange = (index: number, field: 'value' | 'unit', value: string) =>
    updateRowAt(index, row => updateRowField(row, field, value))

  const addItem = () => {
    const key = String(nextKey.current++)
    setRows(prev => [...prev, createEmptyRow(key)])
  }
  const removeItem = (index: number) =>
    setRows(prev => prev.filter((_, i) => i !== index))

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!date) return
    setSaving(true)
    try {
      const validItems = toSavableItems(rows)
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

          {rows.map((row, i) => (
            <div key={row.key} className={`item-row ${row.item.isAbnormal ? 'item-abnormal' : ''}`}>
              <div className="item-fields">
                <div className="item-field-name">
                  {masters.length > 0 ? (
                    <select
                      value={row.isCustom ? CUSTOM_ITEM_OPTION_VALUE : row.item.itemName}
                      onChange={e => handleItemNameSelect(i, e.target.value)}
                    >
                      <option value="">項目を選択</option>
                      {masters.map(m => (
                        <option key={m.itemName} value={m.itemName}>{m.itemName}</option>
                      ))}
                      <option value={CUSTOM_ITEM_OPTION_VALUE}>直接入力</option>
                    </select>
                  ) : (
                    <input
                      type="text"
                      value={row.item.itemName}
                      onChange={e => handleCustomNameChange(i, e.target.value)}
                      placeholder="項目名"
                    />
                  )}
                  {row.isCustom && (
                    <input
                      type="text"
                      value={row.item.itemName}
                      placeholder="項目名を入力"
                      onChange={e => handleCustomNameChange(i, e.target.value)}
                      className="custom-name"
                    />
                  )}
                </div>
                <input
                  type="text"
                  value={row.item.value}
                  onChange={e => handleFieldChange(i, 'value', e.target.value)}
                  placeholder="値"
                  className="item-field-value"
                />
                <input
                  type="text"
                  value={row.item.unit}
                  onChange={e => handleFieldChange(i, 'unit', e.target.value)}
                  placeholder="単位"
                  className="item-field-unit"
                />
              </div>
              <button
                type="button"
                className="btn-remove"
                onClick={() => removeItem(i)}
                disabled={rows.length === 1}
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
