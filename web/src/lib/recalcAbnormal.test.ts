import { describe, expect, it } from 'vitest'
import {
  evaluateAbnormal,
  recalcRecordsForMaster,
  referenceRangeChanged,
  toDoubleOrNull,
} from './recalcAbnormal'
import type { ExaminationRecord } from '../types'

function record(
  id: string,
  items: Array<{
    itemName: string
    value: string
    unit?: string
    referenceMin?: number | null
    referenceMax?: number | null
    isAbnormal?: boolean
  }>,
): ExaminationRecord {
  return {
    id,
    date: '2026-01-01',
    facility: '',
    createdAt: 0,
    items: items.map(i => ({
      itemName: i.itemName,
      value: i.value,
      unit: i.unit ?? '',
      referenceMin: i.referenceMin ?? null,
      referenceMax: i.referenceMax ?? null,
      isAbnormal: i.isAbnormal ?? false,
    })),
  }
}

describe('toDoubleOrNull', () => {
  it('数値文字列を数値化する', () => {
    expect(toDoubleOrNull('22.5')).toBe(22.5)
    expect(toDoubleOrNull('-3.2')).toBe(-3.2)
  })

  it('前後の空白は無視する', () => {
    expect(toDoubleOrNull(' 120 ')).toBe(120)
  })

  it('数値化できない文字列は null を返す（陰性など）', () => {
    expect(toDoubleOrNull('陰性')).toBeNull()
  })

  it('空文字は null を返す', () => {
    expect(toDoubleOrNull('')).toBeNull()
  })

  it('部分的にしか数値でない文字列は null を返す（parseFloat とは異なる挙動）', () => {
    expect(toDoubleOrNull('12kg')).toBeNull()
  })
})

describe('evaluateAbnormal（Android HealthRepository.evaluateAbnormal と同じ結果になること）', () => {
  it('下限未満は true', () => {
    expect(evaluateAbnormal('60', 70, 139)).toBe(true)
  })

  it('上限超過は true', () => {
    expect(evaluateAbnormal('160', 70, 139)).toBe(true)
  })

  it('範囲内は false', () => {
    expect(evaluateAbnormal('100', 70, 139)).toBe(false)
  })

  it('境界値（下限・上限そのもの）は範囲内として false', () => {
    expect(evaluateAbnormal('70', 70, 139)).toBe(false)
    expect(evaluateAbnormal('139', 70, 139)).toBe(false)
  })

  it('数値でない値は false', () => {
    expect(evaluateAbnormal('陰性', 70, 139)).toBe(false)
  })

  it('基準値が両方 null の場合は常に false', () => {
    expect(evaluateAbnormal('999', null, null)).toBe(false)
  })

  it('下限のみ設定されている場合、上限チェックは行わない', () => {
    expect(evaluateAbnormal('99999', 70, null)).toBe(false)
    expect(evaluateAbnormal('1', 70, null)).toBe(true)
  })

  it('上限のみ設定されている場合、下限チェックは行わない', () => {
    expect(evaluateAbnormal('-99999', null, 139)).toBe(false)
    expect(evaluateAbnormal('999', null, 139)).toBe(true)
  })
})

describe('referenceRangeChanged（Android HealthRepository.referenceRangeChanged と同じ結果になること）', () => {
  it('referenceMin が変化した場合は true', () => {
    const previous = { referenceMin: 70, referenceMax: 139 }
    const updated = { referenceMin: 80, referenceMax: 139 }
    expect(referenceRangeChanged(previous, updated)).toBe(true)
  })

  it('referenceMax が変化した場合は true', () => {
    const previous = { referenceMin: 70, referenceMax: 139 }
    const updated = { referenceMin: 70, referenceMax: 150 }
    expect(referenceRangeChanged(previous, updated)).toBe(true)
  })

  it('基準値が変化していない場合は false（単位のみ変更など）', () => {
    const previous = { referenceMin: 70, referenceMax: 139 }
    const updated = { referenceMin: 70, referenceMax: 139 }
    expect(referenceRangeChanged(previous, updated)).toBe(false)
  })

  it('previous が null（新規登録）で基準値が設定される場合は true', () => {
    expect(referenceRangeChanged(null, { referenceMin: 70, referenceMax: 139 })).toBe(true)
  })

  it('previous が null で更新後も基準値が両方 null の場合は false', () => {
    expect(referenceRangeChanged(null, { referenceMin: null, referenceMax: null })).toBe(false)
  })
})

describe('recalcRecordsForMaster', () => {
  it('該当項目名の referenceMin/referenceMax/isAbnormal を新基準で再計算する', () => {
    const records = [
      record('r1', [{ itemName: 'LDL', value: '160', referenceMin: 0, referenceMax: 139, isAbnormal: true }]),
    ]
    const results = recalcRecordsForMaster(records, 'LDL', 0, 180)
    expect(results).toHaveLength(1)
    expect(results[0].recordId).toBe('r1')
    expect(results[0].items[0]).toMatchObject({ referenceMin: 0, referenceMax: 180, isAbnormal: false })
  })

  it('該当しない項目名の行には触れない', () => {
    const records = [
      record('r1', [
        { itemName: 'LDL', value: '160', referenceMin: 0, referenceMax: 139, isAbnormal: true },
        { itemName: '血圧', value: '120', referenceMin: 70, referenceMax: 130, isAbnormal: false },
      ]),
    ]
    const results = recalcRecordsForMaster(records, 'LDL', 0, 200)
    expect(results).toHaveLength(1)
    expect(results[0].items.find(i => i.itemName === '血圧')).toEqual(
      records[0].items.find(i => i.itemName === '血圧'),
    )
  })

  it('再計算しても値に変化がない記録は結果に含めない', () => {
    const records = [
      record('r1', [{ itemName: 'LDL', value: '100', referenceMin: 0, referenceMax: 139, isAbnormal: false }]),
    ]
    // 基準値は同じ 0/139 のまま呼び出しても、対象記録のフィールドが既に一致していれば更新不要
    const results = recalcRecordsForMaster(records, 'LDL', 0, 139)
    expect(results).toEqual([])
  })

  it('該当項目名を含まない記録は結果に含めない', () => {
    const records = [record('r1', [{ itemName: '血圧', value: '120' }])]
    expect(recalcRecordsForMaster(records, 'LDL', 0, 139)).toEqual([])
  })

  it('複数記録のうち、実際に値が変化するものだけを返す', () => {
    const records = [
      // 既に新基準 (0/150) と同じ値が保存されている記録 → 再計算しても変化なし
      record('r1', [{ itemName: 'LDL', value: '100', referenceMin: 0, referenceMax: 150, isAbnormal: false }]),
      // 旧基準 (0/200) のままの記録 → 新基準 (0/150) では referenceMax・isAbnormal ともに変わる
      record('r2', [{ itemName: 'LDL', value: '160', referenceMin: 0, referenceMax: 200, isAbnormal: false }]),
    ]
    const results = recalcRecordsForMaster(records, 'LDL', 0, 150)
    expect(results.map(r => r.recordId)).toEqual(['r2'])
    expect(results[0].items[0]).toMatchObject({ referenceMax: 150, isAbnormal: true })
  })

  it('数値でない値の項目は基準値変更後も isAbnormal=false のまま（変化なし扱い）', () => {
    const records = [
      record('r1', [{ itemName: 'HBs抗原', value: '陰性', referenceMin: null, referenceMax: null, isAbnormal: false }]),
    ]
    const results = recalcRecordsForMaster(records, 'HBs抗原', 0, 1)
    // referenceMin/referenceMax 自体は null -> 0/1 に変わるため更新対象にはなる
    expect(results).toHaveLength(1)
    expect(results[0].items[0]).toMatchObject({ referenceMin: 0, referenceMax: 1, isAbnormal: false })
  })
})
