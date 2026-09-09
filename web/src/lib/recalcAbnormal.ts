// Issue #50: Web 版でも項目マスターの基準値変更を既存記録に再計算して反映するためのロジック。
// Android の HealthRepository.evaluateAbnormal() / referenceRangeChanged()
// （android/app/src/main/java/com/rokusoudo/healthcheckup/data/repository/HealthRepository.kt:312-327）
// と同じ判定結果になるように実装している。Firestore アクセスとは分離した純関数として切り出すことで、
// 将来 Cloud Functions 側での再計算に寄せる場合にも判定ロジックをそのまま流用できるようにしている。
import type { ExaminationItem, ExaminationRecord, ItemMaster } from '../types'

/**
 * Kotlin の String.toDoubleOrNull() に相当する判定を行う。
 * `parseFloat` のような部分一致（例: "12kg" -> 12）は許容せず、文字列全体（前後の空白を除く）が
 * 数値表現である場合のみ変換する。数値化できない値（"陰性" など）は null を返す。
 *
 * 経年グラフ用の `parseNumericValue`（./trend.ts）とは意図的に異なる実装であることに注意。
 * あちらは Issue #28 の決定により parseFloat 相当（部分一致を許容）を採用しているが、
 * 基準値外判定は Android の evaluateAbnormal と完全に一致させる必要があるため、
 * ここでは toDoubleOrNull 相当の厳密な判定を用いる。
 */
export function toDoubleOrNull(raw: string): number | null {
  const trimmed = raw.trim()
  if (trimmed === '') return null
  if (!/^[+-]?(\d+\.?\d*|\.\d+)([eE][+-]?\d+)?$/.test(trimmed)) return null
  const parsed = Number(trimmed)
  return Number.isFinite(parsed) ? parsed : null
}

/**
 * Android の HealthRepository.evaluateAbnormal() と同じ判定を行う。
 * - value が数値化できない場合 → false（基準値外ではない）
 * - min が null → 下限チェックはスキップ
 * - max が null → 上限チェックはスキップ
 * - 下限未満 または 上限超過 → true
 */
export function evaluateAbnormal(value: string, min: number | null, max: number | null): boolean {
  const numericValue = toDoubleOrNull(value)
  if (numericValue === null) return false
  const belowMin = min !== null ? numericValue < min : false
  const aboveMax = max !== null ? numericValue > max : false
  return belowMin || aboveMax
}

/**
 * Android の HealthRepository.referenceRangeChanged() と同じ判定を行う。
 * カテゴリ・お気に入りなど基準値以外の変更では true を返さない
 * （＝呼び出し側で既存記録の全件再計算を避けるための最適化に使う）。
 * previous が null（新規登録）の場合は「未設定」として扱う。
 */
export function referenceRangeChanged(
  previous: Pick<ItemMaster, 'referenceMin' | 'referenceMax'> | null,
  updated: Pick<ItemMaster, 'referenceMin' | 'referenceMax'>,
): boolean {
  const prevMin = previous ? previous.referenceMin : null
  const prevMax = previous ? previous.referenceMax : null
  return prevMin !== updated.referenceMin || prevMax !== updated.referenceMax
}

/** 再計算の結果、Firestore へ書き戻す必要がある記録1件分。 */
export interface RecordRecalcResult {
  recordId: string
  items: ExaminationItem[]
}

/**
 * 指定した項目名 (itemName) を含む記録を走査し、新しい基準値で
 * referenceMin / referenceMax / isAbnormal を再計算する。
 * 実際に値が変化した記録のみを結果に含める（変化がない記録は対象外＝不要な書き込みを避ける）。
 *
 * Android の upsertMaster() 内の再計算処理
 * （existingItems を新基準で map し直す部分）に相当する。
 */
export function recalcRecordsForMaster(
  records: ExaminationRecord[],
  itemName: string,
  referenceMin: number | null,
  referenceMax: number | null,
): RecordRecalcResult[] {
  const results: RecordRecalcResult[] = []

  for (const record of records) {
    let changed = false
    const items = record.items.map(item => {
      if (item.itemName !== itemName) return item

      const isAbnormal = evaluateAbnormal(item.value, referenceMin, referenceMax)
      if (
        item.referenceMin === referenceMin &&
        item.referenceMax === referenceMax &&
        item.isAbnormal === isAbnormal
      ) {
        return item
      }

      changed = true
      return { ...item, referenceMin, referenceMax, isAbnormal }
    })

    if (changed) {
      results.push({ recordId: record.id, items })
    }
  }

  return results
}
