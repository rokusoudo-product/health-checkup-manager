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
}
