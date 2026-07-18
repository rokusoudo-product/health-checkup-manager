export interface ExaminationItem {
  itemName: string
  value: string
  unit: string
  referenceMin: number | null
  referenceMax: number | null
  isAbnormal: boolean
}

export interface ExaminationRecord {
  id: string
  date: string       // "yyyy-MM-dd"
  facility: string
  createdAt: number
  items: ExaminationItem[]
}

export interface ItemMaster {
  itemName: string
  unit: string
  referenceMin: number | null
  referenceMax: number | null
  // 2026-07 画面遷移刷新001（Android側）で追加。旧ドキュメントには存在しないため optional
  category?: string
  isFavorite?: boolean
  favoritedAt?: number | null
}
