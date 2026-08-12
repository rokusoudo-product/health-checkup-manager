import { useEffect, useMemo, useState } from 'react'
import { fetchMasters, fetchRecords } from '../firestoreService'
import type { ExaminationRecord, ItemMaster } from '../types'
import { TrendPeriod, buildTrendPoints, filterByPeriod, latestReferenceRange } from '../lib/trend'
import TrendChart from '../components/TrendChart'

interface Props { uid: string }

/**
 * US-W04 / T-601: Web版 経年グラフ画面。
 * 選択した検査項目の値の推移を折れ線グラフで表示する（Android S-04 相当）。
 * 薬事法対応: 医療的な評価・助言・改善提案の文言は一切表示しない（docs/compliance.md 参照）。
 */
export default function TrendGraph({ uid }: Props) {
  const [records, setRecords] = useState<ExaminationRecord[]>([])
  const [masters, setMasters] = useState<ItemMaster[]>([])
  const [loading, setLoading] = useState(true)
  const [itemName, setItemName] = useState('')
  const [period, setPeriod] = useState<TrendPeriod>(TrendPeriod.ALL)

  useEffect(() => {
    Promise.all([fetchRecords(uid), fetchMasters(uid)])
      .then(([r, m]) => {
        setRecords(r)
        setMasters(m)
      })
      .finally(() => setLoading(false))
  }, [uid])

  // 選択可能な項目名: マスター登録済み項目 + 記録に含まれる項目名（マスター未登録でも選べるようにする）
  const itemNames = useMemo(() => {
    const names = new Set<string>()
    masters.forEach(m => names.add(m.itemName))
    records.forEach(r => r.items.forEach(i => names.add(i.itemName)))
    return Array.from(names).sort((a, b) => a.localeCompare(b, 'ja'))
  }, [masters, records])

  // 選択中の項目が存在しない（未選択・記録更新で消えた等）場合は先頭の項目にフォールバックする。
  // useEffect+setState で同期するとカスケード再レンダーになるため、派生値として計算する。
  const selectedItemName = itemNames.includes(itemName) ? itemName : (itemNames[0] ?? '')

  const allPoints = useMemo(
    () => (selectedItemName ? buildTrendPoints(records, selectedItemName) : []),
    [records, selectedItemName],
  )
  const points = useMemo(() => filterByPeriod(allPoints, period), [allPoints, period])
  const { referenceMin, referenceMax } = useMemo(() => latestReferenceRange(allPoints), [allPoints])

  if (loading) return <p className="msg">読み込み中...</p>

  return (
    <div>
      <h2>経年グラフ</h2>
      <p className="note-small">検査項目を選択すると、記録済みの値の推移を確認できます。</p>

      {itemNames.length === 0 ? (
        <p className="msg">記録がありません。「記録一覧」から登録してください。</p>
      ) : (
        <>
          <div className="trend-controls">
            <select
              className="trend-item-select"
              value={selectedItemName}
              onChange={e => setItemName(e.target.value)}
              aria-label="検査項目"
            >
              {itemNames.map(name => (
                <option key={name} value={name}>{name}</option>
              ))}
            </select>

            <div className="trend-period-tabs" role="tablist" aria-label="期間">
              <button
                type="button"
                role="tab"
                aria-selected={period === TrendPeriod.ONE_YEAR}
                className={period === TrendPeriod.ONE_YEAR ? 'trend-tab active' : 'trend-tab'}
                onClick={() => setPeriod(TrendPeriod.ONE_YEAR)}
              >
                1年
              </button>
              <button
                type="button"
                role="tab"
                aria-selected={period === TrendPeriod.ALL}
                className={period === TrendPeriod.ALL ? 'trend-tab active' : 'trend-tab'}
                onClick={() => setPeriod(TrendPeriod.ALL)}
              >
                全期間
              </button>
            </div>
          </div>

          {points.length === 0 ? (
            <p className="msg">この項目・期間で表示できる記録がありません。</p>
          ) : (
            <TrendChart
              itemName={selectedItemName}
              labels={points.map(p => p.date)}
              values={points.map(p => p.value)}
              unit={points[points.length - 1].unit}
              referenceMin={referenceMin}
              referenceMax={referenceMax}
            />
          )}
        </>
      )}
    </div>
  )
}
