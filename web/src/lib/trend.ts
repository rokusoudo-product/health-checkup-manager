// Issue #28: Web 版経年グラフのロジック（数値変換・期間フィルタ）を UI から分離したモジュール。
// Android の ui/graph/TrendPeriodFilter.kt・TrendGraphFragment.kt と同じ結果になるよう実装している。
// 相違点（PR本文にも明記）:
//   - 数値変換は Issue #28 の決定により Android の toDoubleOrNull ではなく parseFloat 相当を採用する
//     （"12abc" のような文字列も 12 として数値化される点が toDoubleOrNull と異なる）
//   - 基準値の参照線は「直近の記録で referenceMin/Max が設定されている値」を使う
//     （Android は経年データの先頭行=最も古い記録の値を使っており、そちらは実質バグに近いため web では最新値を採用）
import type { ExaminationRecord } from '../types'

/** S-04 グラフの期間切替（Android TrendPeriod と同じ「1年 / 全期間」の2値） */
export const TrendPeriod = {
  ONE_YEAR: 'ONE_YEAR',
  ALL: 'ALL',
} as const

export type TrendPeriod = (typeof TrendPeriod)[keyof typeof TrendPeriod]

/** 検査項目1件・記録1件分のグラフ用データ点 */
export interface TrendPoint {
  date: string // "yyyy-MM-dd"
  value: number
  unit: string
  referenceMin: number | null
  referenceMax: number | null
}

/**
 * 検査値の文字列を数値化する。
 * 「陰性」など数値化できない値は null を返し、呼び出し側でグラフから除外する。
 * Issue #28 の決定: Android の toDoubleOrNull ではなく parseFloat 相当で判定する。
 */
export function parseNumericValue(raw: string): number | null {
  const parsed = parseFloat(raw)
  return Number.isNaN(parsed) ? null : parsed
}

/**
 * 診断記録の一覧から、指定した検査項目名のグラフ用データ点を抽出する。
 * 数値化できない値は除外し、日付昇順（古い→新しい）に並べる。
 */
export function buildTrendPoints(records: ExaminationRecord[], itemName: string): TrendPoint[] {
  const points: TrendPoint[] = []
  for (const record of records) {
    const item = record.items.find(i => i.itemName === itemName)
    if (!item) continue
    const value = parseNumericValue(item.value)
    if (value === null) continue
    points.push({
      date: record.date,
      value,
      unit: item.unit,
      referenceMin: item.referenceMin,
      referenceMax: item.referenceMax,
    })
  }
  return [...points].sort((a, b) => a.date.localeCompare(b.date))
}

interface CalendarDate {
  y: number
  m: number // 1-12
  d: number
}

/** "yyyy-MM-dd" を厳密に検証してパースする（LocalDate.parse と同様に不正な日付は null） */
function parseIsoDate(value: string): CalendarDate | null {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value)
  if (!match) return null
  const y = Number(match[1])
  const m = Number(match[2])
  const d = Number(match[3])
  const roundTrip = new Date(y, m - 1, d)
  if (roundTrip.getFullYear() !== y || roundTrip.getMonth() !== m - 1 || roundTrip.getDate() !== d) {
    return null
  }
  return { y, m, d }
}

function isLeapYear(y: number): boolean {
  return (y % 4 === 0 && y % 100 !== 0) || y % 400 === 0
}

/** LocalDate.minusYears(1) と同じ挙動（うるう日は非うるう年では2/28にクランプ） */
function oneYearBefore(date: CalendarDate): CalendarDate {
  const y = date.y - 1
  if (date.m === 2 && date.d === 29 && !isLeapYear(y)) {
    return { y, m: 2, d: 28 }
  }
  return { y, m: date.m, d: date.d }
}

function dateKey(date: CalendarDate): number {
  return date.y * 10000 + date.m * 100 + date.d
}

/**
 * 期間でグラフ用データ点を絞り込む。
 * ALL: すべて返す。ONE_YEAR: 今日から1年以内（境界日を含む）のみ返し、
 * 日付として解釈できない行は除外する（Android TrendPeriodFilter.filter と同じ）。
 */
export function filterByPeriod(
  points: TrendPoint[],
  period: TrendPeriod,
  today: Date = new Date(),
): TrendPoint[] {
  if (period === TrendPeriod.ALL) return points

  const cutoff = oneYearBefore({ y: today.getFullYear(), m: today.getMonth() + 1, d: today.getDate() })
  const cutoffKey = dateKey(cutoff)

  return points.filter(point => {
    const parsed = parseIsoDate(point.date)
    return parsed !== null && dateKey(parsed) >= cutoffKey
  })
}

/**
 * 参照線用の基準値を返す。referenceMin/Max が設定されている直近の記録の値を採用する。
 * すべて未設定の場合は null/null を返す（呼び出し側で参照線を描画しない）。
 */
export function latestReferenceRange(points: TrendPoint[]): {
  referenceMin: number | null
  referenceMax: number | null
} {
  for (let i = points.length - 1; i >= 0; i -= 1) {
    const point = points[i]
    if (point.referenceMin !== null || point.referenceMax !== null) {
      return { referenceMin: point.referenceMin, referenceMax: point.referenceMax }
    }
  }
  return { referenceMin: null, referenceMax: null }
}
