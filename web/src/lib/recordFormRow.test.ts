import { describe, expect, it } from 'vitest'
import {
  CUSTOM_ITEM_OPTION_VALUE,
  createEmptyRow,
  selectItemName,
  setCustomItemName,
  toSavableItems,
  updateRowField,
  type FormItemRow,
} from './recordFormRow'
import type { ItemMaster } from '../types'

const masters: ItemMaster[] = [
  { itemName: 'LDL', unit: 'mg/dL', referenceMin: 0, referenceMax: 139 },
  { itemName: '血圧', unit: 'mmHg', referenceMin: 70, referenceMax: 130 },
]

describe('createEmptyRow', () => {
  it('空の行を作る（isCustom は false）', () => {
    const row = createEmptyRow('k1')
    expect(row.key).toBe('k1')
    expect(row.isCustom).toBe(false)
    expect(row.item.itemName).toBe('')
  })
})

describe('selectItemName', () => {
  it('「直接入力」を選ぶと isCustom=true になり、itemName は空文字にリセットされる', () => {
    const row = createEmptyRow('k1')
    const next = selectItemName(row, CUSTOM_ITEM_OPTION_VALUE, masters)
    expect(next.isCustom).toBe(true)
    expect(next.item.itemName).toBe('')
  })

  it('「直接入力」を選んだ時点では CUSTOM_ITEM_OPTION_VALUE が itemName に入らない', () => {
    const row = createEmptyRow('k1')
    const next = selectItemName(row, CUSTOM_ITEM_OPTION_VALUE, masters)
    expect(next.item.itemName).not.toBe(CUSTOM_ITEM_OPTION_VALUE)
  })

  it('マスターの項目名を選ぶと基準値・単位が補完され、isCustom は false になる', () => {
    const row: FormItemRow = { ...createEmptyRow('k1'), isCustom: true }
    const next = selectItemName(row, 'LDL', masters)
    expect(next.isCustom).toBe(false)
    expect(next.item.itemName).toBe('LDL')
    expect(next.item.unit).toBe('mg/dL')
    expect(next.item.referenceMin).toBe(0)
    expect(next.item.referenceMax).toBe(139)
  })

  it('マスターにない値を選んだ場合（"項目を選択" =空文字など）は基準値を補完しない', () => {
    const row = createEmptyRow('k1')
    const next = selectItemName(row, '', masters)
    expect(next.item.itemName).toBe('')
    expect(next.item.unit).toBe('')
  })
})

describe('setCustomItemName（直接入力欄の制御コンポーネント化）', () => {
  it('1文字ずつ入力しても isCustom は true のまま保たれ、itemName が更新される', () => {
    let row = selectItemName(createEmptyRow('k1'), CUSTOM_ITEM_OPTION_VALUE, masters)
    row = setCustomItemName(row, '随')
    expect(row.isCustom).toBe(true)
    expect(row.item.itemName).toBe('随')

    row = setCustomItemName(row, '随時')
    expect(row.isCustom).toBe(true)
    expect(row.item.itemName).toBe('随時')

    row = setCustomItemName(row, '随時血糖')
    expect(row.isCustom).toBe(true)
    expect(row.item.itemName).toBe('随時血糖')
  })

  it('複数文字を最後まで入力できる（1文字目で消えない）', () => {
    let row = selectItemName(createEmptyRow('k1'), CUSTOM_ITEM_OPTION_VALUE, masters)
    for (const ch of '随時血糖') {
      row = setCustomItemName(row, row.item.itemName + ch)
      expect(row.isCustom).toBe(true)
    }
    expect(row.item.itemName).toBe('随時血糖')
  })
})

describe('updateRowField', () => {
  it('itemName 以外のフィールド（value）を更新できる', () => {
    const row = createEmptyRow('k1')
    const next = updateRowField(row, 'value', '100')
    expect(next.item.value).toBe('100')
  })

  it('基準値外の値を入力すると isAbnormal が true になる（既存ロジックを維持）', () => {
    let row = selectItemName(createEmptyRow('k1'), 'LDL', masters)
    row = updateRowField(row, 'value', '160')
    expect(row.item.isAbnormal).toBe(true)
  })

  it('基準値内の値では isAbnormal が false になる', () => {
    let row = selectItemName(createEmptyRow('k1'), 'LDL', masters)
    row = updateRowField(row, 'value', '100')
    expect(row.item.isAbnormal).toBe(false)
  })
})

describe('toSavableItems', () => {
  it('直接入力で入力した項目名がそのまま items[].itemName になる', () => {
    let row = selectItemName(createEmptyRow('k1'), CUSTOM_ITEM_OPTION_VALUE, masters)
    row = setCustomItemName(row, '随時血糖')
    row = updateRowField(row, 'value', '120')
    const items = toSavableItems([row])
    expect(items).toEqual([
      { itemName: '随時血糖', value: '120', unit: '', referenceMin: null, referenceMax: null, isAbnormal: false },
    ])
  })

  it('直接入力を選んで何も入力しなかった行は保存対象から除外される', () => {
    const row = selectItemName(createEmptyRow('k1'), CUSTOM_ITEM_OPTION_VALUE, masters)
    expect(toSavableItems([row])).toEqual([])
  })

  it('CUSTOM_ITEM_OPTION_VALUE がそのまま itemName として保存される経路は存在しない', () => {
    // selectItemName の実装上、通常は起こらないが、万一 itemName に '__custom__' が
    // 混入していたケースを想定した防御的テスト（toSavableItems 側のガード）。
    const row: FormItemRow = {
      key: 'k1',
      isCustom: true,
      item: { itemName: CUSTOM_ITEM_OPTION_VALUE, value: '', unit: '', referenceMin: null, referenceMax: null, isAbnormal: false },
    }
    expect(toSavableItems([row])).toEqual([])
  })

  it('マスターから選択した行と直接入力の行を混在させても、それぞれ正しく保存される', () => {
    let masterRow = selectItemName(createEmptyRow('k1'), 'LDL', masters)
    masterRow = updateRowField(masterRow, 'value', '100')

    let customRow = selectItemName(createEmptyRow('k2'), CUSTOM_ITEM_OPTION_VALUE, masters)
    customRow = setCustomItemName(customRow, 'カスタム項目')
    customRow = updateRowField(customRow, 'value', '5')

    const items = toSavableItems([masterRow, customRow])
    expect(items.map(i => i.itemName)).toEqual(['LDL', 'カスタム項目'])
  })

  it('項目名が空文字の行は除外する（既存の挙動を維持）', () => {
    const row = createEmptyRow('k1')
    expect(toSavableItems([row])).toEqual([])
  })
})

describe('行の追加・削除でモードがずれないこと（key ベースで管理しているため配列操作の影響を受けない）', () => {
  it('複数行のうち特定の行だけを直接入力モードにしても、他の行の状態は変わらない', () => {
    const rowA = selectItemName(createEmptyRow('a'), 'LDL', masters)
    const rowB = selectItemName(createEmptyRow('b'), CUSTOM_ITEM_OPTION_VALUE, masters)
    const rowC = createEmptyRow('c')
    const rows = [rowA, rowB, rowC]

    expect(rows.map(r => r.isCustom)).toEqual([false, true, false])
  })

  it('先頭の行を削除しても、残りの行は自分の key に紐づく isCustom を保ったまま（インデックスに依存しない）', () => {
    const rowA = selectItemName(createEmptyRow('a'), 'LDL', masters)
    const rowB = selectItemName(createEmptyRow('b'), CUSTOM_ITEM_OPTION_VALUE, masters)
    const rowC = createEmptyRow('c')
    const rows = [rowA, rowB, rowC]

    // rowA（先頭、インデックス0）を削除 -> 削除前は rowB がインデックス1、削除後はインデックス0になる
    const afterRemove = rows.filter(r => r.key !== 'a')

    // インデックスは変わるが、isCustom はそれぞれの行オブジェクトに紐づいているため
    // rowB（今はインデックス0）が isCustom=true のまま、rowC（今はインデックス1）は false のまま
    expect(afterRemove.map(r => r.key)).toEqual(['b', 'c'])
    expect(afterRemove.find(r => r.key === 'b')?.isCustom).toBe(true)
    expect(afterRemove.find(r => r.key === 'c')?.isCustom).toBe(false)
  })

  it('行を追加しても既存行の isCustom は変わらない', () => {
    const rowA = selectItemName(createEmptyRow('a'), CUSTOM_ITEM_OPTION_VALUE, masters)
    const rows = [rowA, createEmptyRow('b')]
    expect(rows.find(r => r.key === 'a')?.isCustom).toBe(true)
  })
})
