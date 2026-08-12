import { describe, expect, it } from 'vitest'
import {
  TrendPeriod,
  buildTrendPoints,
  filterByPeriod,
  latestReferenceRange,
  parseNumericValue,
} from './trend'
import type { ExaminationRecord } from '../types'

function record(
  date: string,
  itemName: string,
  value: string,
  overrides: Partial<{ unit: string; referenceMin: number | null; referenceMax: number | null }> = {},
): ExaminationRecord {
  return {
    id: date,
    date,
    facility: '',
    createdAt: 0,
    items: [
      {
        itemName,
        value,
        unit: overrides.unit ?? '',
        referenceMin: overrides.referenceMin ?? null,
        referenceMax: overrides.referenceMax ?? null,
        isAbnormal: false,
      },
    ],
  }
}

describe('parseNumericValue', () => {
  it('数値文字列を数値化する', () => {
    expect(parseNumericValue('22.5')).toBe(22.5)
  })

  it('数値化できない文字列は null を返す', () => {
    expect(parseNumericValue('陰性')).toBeNull()
  })

  it('空文字は null を返す', () => {
    expect(parseNumericValue('')).toBeNull()
  })

  it('Issue #28 の決定: parseFloat 相当のため先頭が数値なら変換できる', () => {
    expect(parseNumericValue('12kg')).toBe(12)
  })
})

describe('buildTrendPoints', () => {
  it('指定した項目名のデータ点のみを日付昇順で抽出する', () => {
    const records: ExaminationRecord[] = [
      record('2026-06-01', 'BMI', '22.0'),
      record('2025-06-01', 'BMI', '21.0'),
      record('2025-06-01', '血圧', '120'),
    ]
    const points = buildTrendPoints(records, 'BMI')
    expect(points.map(p => p.date)).toEqual(['2025-06-01', '2026-06-01'])
    expect(points.map(p => p.value)).toEqual([21.0, 22.0])
  })

  it('数値化できない値は除外する', () => {
    const records: ExaminationRecord[] = [
      record('2026-06-01', 'HBs抗原', '陰性'),
      record('2026-07-01', 'HBs抗原', '1.2'),
    ]
    const points = buildTrendPoints(records, 'HBs抗原')
    expect(points.map(p => p.date)).toEqual(['2026-07-01'])
  })

  it('記録0件のときは空配列を返す', () => {
    expect(buildTrendPoints([], 'BMI')).toEqual([])
  })

  it('該当項目が0件のときは空配列を返す', () => {
    const records: ExaminationRecord[] = [record('2026-06-01', '血圧', '120')]
    expect(buildTrendPoints(records, 'BMI')).toEqual([])
  })
})

describe('filterByPeriod', () => {
  const today = new Date(2026, 6, 19) // 2026-07-19（Android側テストと同じ基準日）

  it('全期間はすべてのデータを返す', () => {
    const points = buildTrendPoints(
      [record('2020-01-01', 'BMI', '22'), record('2025-06-01', 'BMI', '22'), record('2026-06-01', 'BMI', '22')],
      'BMI',
    )
    expect(filterByPeriod(points, TrendPeriod.ALL, today)).toHaveLength(3)
  })

  it('1年は今日から1年以内（境界日を含む）のデータのみ返す', () => {
    const points = buildTrendPoints(
      [
        record('2020-01-01', 'BMI', '22'),
        record('2025-07-18', 'BMI', '22'), // 1年+1日前 → 除外
        record('2025-07-19', 'BMI', '22'), // ちょうど1年前 → 含む
        record('2026-06-01', 'BMI', '22'),
      ],
      'BMI',
    )
    const filtered = filterByPeriod(points, TrendPeriod.ONE_YEAR, today)
    expect(filtered.map(p => p.date)).toEqual(['2025-07-19', '2026-06-01'])
  })

  it('日付として解釈できない行は1年フィルタで除外される', () => {
    const points = [
      { date: '不正な日付', value: 22, unit: '', referenceMin: null, referenceMax: null },
      { date: '2026-06-01', value: 22, unit: '', referenceMin: null, referenceMax: null },
    ]
    const filtered = filterByPeriod(points, TrendPeriod.ONE_YEAR, today)
    expect(filtered.map(p => p.date)).toEqual(['2026-06-01'])
  })

  it('うるう日の1年前は非うるう年では2/28にクランプする', () => {
    const leapDayToday = new Date(2028, 1, 29) // 2028-02-29（うるう年）
    const points = [
      { date: '2027-02-28', value: 1, unit: '', referenceMin: null, referenceMax: null },
      { date: '2027-03-01', value: 1, unit: '', referenceMin: null, referenceMax: null },
    ]
    const filtered = filterByPeriod(points, TrendPeriod.ONE_YEAR, leapDayToday)
    expect(filtered.map(p => p.date)).toEqual(['2027-02-28', '2027-03-01'])
  })
})

describe('latestReferenceRange', () => {
  it('直近の記録の基準値を返す', () => {
    const points = [
      { date: '2025-01-01', value: 1, unit: '', referenceMin: 1, referenceMax: 2 },
      { date: '2026-01-01', value: 1, unit: '', referenceMin: 3, referenceMax: 4 },
    ]
    expect(latestReferenceRange(points)).toEqual({ referenceMin: 3, referenceMax: 4 })
  })

  it('直近の記録に基準値がない場合は遡って探す', () => {
    const points = [
      { date: '2025-01-01', value: 1, unit: '', referenceMin: 1, referenceMax: 2 },
      { date: '2026-01-01', value: 1, unit: '', referenceMin: null, referenceMax: null },
    ]
    expect(latestReferenceRange(points)).toEqual({ referenceMin: 1, referenceMax: 2 })
  })

  it('基準値が1件もない場合は null/null を返す', () => {
    const points = [{ date: '2025-01-01', value: 1, unit: '', referenceMin: null, referenceMax: null }]
    expect(latestReferenceRange(points)).toEqual({ referenceMin: null, referenceMax: null })
  })

  it('データなしの場合は null/null を返す', () => {
    expect(latestReferenceRange([])).toEqual({ referenceMin: null, referenceMax: null })
  })
})
