import { describe, expect, it } from 'vitest'
import { buildAbnormalEntries, formatReferenceRange } from './abnormal'
import type { ExaminationRecord } from '../types'

function record(
  id: string,
  date: string,
  facility: string,
  items: Array<{
    itemName: string
    value: string
    unit?: string
    referenceMin?: number | null
    referenceMax?: number | null
    isAbnormal: boolean
  }>,
): ExaminationRecord {
  return {
    id,
    date,
    facility,
    createdAt: 0,
    items: items.map(i => ({
      itemName: i.itemName,
      value: i.value,
      unit: i.unit ?? '',
      referenceMin: i.referenceMin ?? null,
      referenceMax: i.referenceMax ?? null,
      isAbnormal: i.isAbnormal,
    })),
  }
}

describe('buildAbnormalEntries', () => {
  it('isAbnormal===true の項目のみを抽出する', () => {
    const records: ExaminationRecord[] = [
      record('r1', '2026-06-01', 'A病院', [
        { itemName: 'BMI', value: '22.0', isAbnormal: false },
        { itemName: 'LDL', value: '160', unit: 'mg/dL', isAbnormal: true },
      ]),
    ]
    const entries = buildAbnormalEntries(records)
    expect(entries).toHaveLength(1)
    expect(entries[0].itemName).toBe('LDL')
  })

  it('検査日の新しい順（降順）に並べる', () => {
    const records: ExaminationRecord[] = [
      record('r1', '2025-01-01', 'A病院', [{ itemName: 'X', value: '1', isAbnormal: true }]),
      record('r2', '2026-06-01', 'B病院', [{ itemName: 'Y', value: '2', isAbnormal: true }]),
      record('r3', '2025-12-01', 'C病院', [{ itemName: 'Z', value: '3', isAbnormal: true }]),
    ]
    const entries = buildAbnormalEntries(records)
    expect(entries.map(e => e.date)).toEqual(['2026-06-01', '2025-12-01', '2025-01-01'])
  })

  it('項目単位のフラット表示: 同一記録内の複数の基準値外項目は個別の行になる', () => {
    const records: ExaminationRecord[] = [
      record('r1', '2026-06-01', 'A病院', [
        { itemName: 'LDL', value: '160', isAbnormal: true },
        { itemName: '血圧', value: '145', isAbnormal: true },
      ]),
    ]
    const entries = buildAbnormalEntries(records)
    expect(entries).toHaveLength(2)
    expect(entries.every(e => e.recordId === 'r1')).toBe(true)
  })

  it('基準値外が0件のとき空配列を返す', () => {
    const records: ExaminationRecord[] = [
      record('r1', '2026-06-01', 'A病院', [{ itemName: 'BMI', value: '22.0', isAbnormal: false }]),
    ]
    expect(buildAbnormalEntries(records)).toEqual([])
  })

  it('records が空配列でもエラーにならず空配列を返す', () => {
    expect(buildAbnormalEntries([])).toEqual([])
  })
})

describe('formatReferenceRange', () => {
  it('上限・下限がともにある場合はレンジ表記', () => {
    expect(formatReferenceRange(70, 139)).toBe('70 〜 139')
  })

  it('下限のみの場合、上限側は—', () => {
    expect(formatReferenceRange(70, null)).toBe('70 〜 —')
  })

  it('両方 null の場合は—', () => {
    expect(formatReferenceRange(null, null)).toBe('—')
  })
})
