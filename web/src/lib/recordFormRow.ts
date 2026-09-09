// Issue #51: RecordForm の検査項目行1件分の状態遷移を、React コンポーネントから切り出した純関数群。
// 「直接入力を選んでいる」という UI 状態（isCustom）を item.itemName とは独立に保持することで、
// 次の2つの不具合を構造的に起こり得なくする。
//   1. 1文字入力した時点で直接入力欄がアンマウントされ、入力できなくなる
//      （旧実装は item.itemName === '__custom__' を表示条件にしていたため）
//   2. '__custom__' という一時的な選択値がそのまま項目名として Firestore に保存される
//
// 行の識別には配列インデックスではなく安定した `key`（RecordForm 側で行の追加時に採番し、
// 削除時もその行の key ごと配列から取り除かれるだけ）を使う。isCustom は各行オブジェクト自身に
// 紐づくフラグなので、他の行の追加・削除の影響を受けない（インデックスのずれが起こり得ない）。
import type { ExaminationItem, ItemMaster } from '../types'

/** 項目名 <select> で「直接入力」を選んだことを表す一時的な値。item.itemName には絶対に入らない。 */
export const CUSTOM_ITEM_OPTION_VALUE = '__custom__'

/** RecordForm の検査項目1行分の状態。 */
export interface FormItemRow {
  /** 行の安定識別子。追加・削除しても他の行とずれないようにするためのキー（配列インデックスは使わない） */
  key: string
  /** Firestore に保存される実体 */
  item: ExaminationItem
  /** 「直接入力モード」かどうかを表す表示専用フラグ。item / Firestore には保存されない */
  isCustom: boolean
}

export function createEmptyItem(): ExaminationItem {
  return {
    itemName: '', value: '', unit: '',
    referenceMin: null, referenceMax: null, isAbnormal: false,
  }
}

export function createEmptyRow(key: string): FormItemRow {
  return { key, item: createEmptyItem(), isCustom: false }
}

/** 既存の updateItem 内にあった isAbnormal 再計算ロジック（挙動を変えずに切り出したもの）。 */
function recalcAbnormal(item: ExaminationItem): ExaminationItem {
  const num = parseFloat(item.value)
  const isAbnormal = !isNaN(num) && (
    (item.referenceMin != null && num < item.referenceMin) ||
    (item.referenceMax != null && num > item.referenceMax)
  )
  return { ...item, isAbnormal }
}

/**
 * 項目名 <select> の onChange 用。
 * - '__custom__' が選ばれた場合: isCustom=true にし、itemName はマスターから引き継がず空文字にリセットする。
 *   これにより '__custom__' という文字列が item.itemName に入ることは構造上あり得ない。
 * - マスターの項目名が選ばれた場合: isCustom=false に戻し、基準値をマスターから補完する（既存の挙動）。
 */
export function selectItemName(row: FormItemRow, value: string, masters: ItemMaster[]): FormItemRow {
  if (value === CUSTOM_ITEM_OPTION_VALUE) {
    return {
      ...row,
      isCustom: true,
      item: recalcAbnormal({
        ...row.item,
        itemName: '',
        unit: '',
        referenceMin: null,
        referenceMax: null,
      }),
    }
  }

  const item = { ...row.item, itemName: value }
  const master = masters.find(m => m.itemName === value)
  if (master) {
    item.unit = master.unit
    item.referenceMin = master.referenceMin
    item.referenceMax = master.referenceMax
  }
  return { ...row, isCustom: false, item: recalcAbnormal(item) }
}

/** 直接入力欄（制御コンポーネント）の onChange 用。isCustom は変えず itemName だけを更新する。 */
export function setCustomItemName(row: FormItemRow, value: string): FormItemRow {
  return { ...row, item: recalcAbnormal({ ...row.item, itemName: value }) }
}

/** 項目名以外のフィールド（値・単位・基準値）更新用。既存の updateItem 相当。 */
export function updateRowField(
  row: FormItemRow,
  field: Exclude<keyof ExaminationItem, 'itemName' | 'isAbnormal'>,
  value: string,
): FormItemRow {
  const item = { ...row.item }
  if (field === 'referenceMin' || field === 'referenceMax') {
    item[field] = value === '' ? null : Number(value)
  } else {
    item[field] = value
  }
  return { ...row, item: recalcAbnormal(item) }
}

/**
 * 保存対象の ExaminationItem[] へ変換する。
 * 項目名が空（trim 後）の行は除外する（既存の挙動を維持）。
 * selectItemName の実装により item.itemName に CUSTOM_ITEM_OPTION_VALUE が入ることはないが、
 * 万一混入していたとしても trim() では空文字にならず除外されない点は仕様上の防御線として
 * toSavableItemsRejectsCustomMarker のテストで別途保証する。
 */
export function toSavableItems(rows: FormItemRow[]): ExaminationItem[] {
  return rows
    .map(r => r.item)
    .filter(i => i.itemName.trim() && i.itemName !== CUSTOM_ITEM_OPTION_VALUE)
}
