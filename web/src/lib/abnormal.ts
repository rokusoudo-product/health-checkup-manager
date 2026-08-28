import type { ExaminationRecord } from '../types'

/**
 * 基準値外一覧（US-W05 / T-602）の1行分のデータ。
 * 記録単位ではなく項目単位でフラットに扱う（Issue #33 の前提どおり）。
 */
export interface AbnormalEntry {
  recordId: string
  date: string
  facility: string
  itemName: string
  value: string
  unit: string
  referenceMin: number | null
  referenceMax: number | null
}

/**
 * 記録一覧から isAbnormal===true の項目のみを取り出し、検査日の新しい順（降順）に並べる。
 * 日付は "yyyy-MM-dd" 形式で保存されているため文字列比較でソート可能。
 * 同一日付内の順序は元の records の並び（Firestore からは日付降順で取得）を保つ安定ソート。
 */
export function buildAbnormalEntries(records: ExaminationRecord[]): AbnormalEntry[] {
  const entries: AbnormalEntry[] = []
  for (const r of records) {
    for (const item of r.items) {
      if (!item.isAbnormal) continue
      entries.push({
        recordId: r.id,
        date: r.date,
        facility: r.facility,
        itemName: item.itemName,
        value: item.value,
        unit: item.unit,
        referenceMin: item.referenceMin,
        referenceMax: item.referenceMax,
      })
    }
  }
  return entries.sort((a, b) => (a.date < b.date ? 1 : a.date > b.date ? -1 : 0))
}

/** 基準値レンジの表示用文字列。両方 null の場合は「—」。 */
export function formatReferenceRange(min: number | null, max: number | null): string {
  if (min == null && max == null) return '—'
  return `${min ?? '—'} 〜 ${max ?? '—'}`
}
