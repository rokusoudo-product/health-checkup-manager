import { useEffect, useRef } from 'react'
import {
  CategoryScale,
  Chart,
  Legend,
  LinearScale,
  LineController,
  LineElement,
  PointElement,
  Tooltip,
} from 'chart.js'
import type { ChartDataset } from 'chart.js'

Chart.register(CategoryScale, LinearScale, LineElement, PointElement, LineController, Tooltip, Legend)

/**
 * CSS カスタムプロパティ（DESIGN.md「Web 版トークン適用方針」）の実測値を取得する。
 * Canvas 2D API の strokeStyle/fillStyle は `var(--foo)` を解決できないため、
 * getComputedStyle 経由で実際のカラーコードに変換してから Chart.js に渡す。
 */
function resolveCssColor(varName: string, fallback: string): string {
  if (typeof document === 'undefined') return fallback
  const value = getComputedStyle(document.documentElement).getPropertyValue(varName).trim()
  return value || fallback
}

interface TrendChartProps {
  itemName: string
  labels: string[]
  values: number[]
  unit: string
  referenceMin: number | null
  referenceMax: number | null
}

/**
 * 経年グラフの折れ線表示コンポーネント（Issue #28: Chart.js 採用、後から差し替えやすいよう薄くラップ）。
 * 薬事法対応: 表示するテキストは項目名・単位・日付・数値のみ。医療的な評価・助言の文言は一切表示しない。
 */
export default function TrendChart({ itemName, labels, values, unit, referenceMin, referenceMax }: TrendChartProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const chartRef = useRef<Chart<'line', number[], string> | null>(null)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    const primaryColor = resolveCssColor('--color-primary', '#00696C')
    const errorColor = resolveCssColor('--color-error', '#BA1A1A')

    const datasets: ChartDataset<'line', number[]>[] = [
      {
        label: unit ? `${itemName} (${unit})` : itemName,
        data: values,
        borderColor: primaryColor,
        backgroundColor: primaryColor,
        pointBackgroundColor: primaryColor,
        tension: 0.15,
      },
    ]

    if (referenceMin !== null) {
      datasets.push({
        label: '基準値（下限）',
        data: values.map(() => referenceMin),
        borderColor: errorColor,
        backgroundColor: errorColor,
        pointRadius: 0,
        borderDash: [6, 4],
      })
    }
    if (referenceMax !== null) {
      datasets.push({
        label: '基準値（上限）',
        data: values.map(() => referenceMax),
        borderColor: errorColor,
        backgroundColor: errorColor,
        pointRadius: 0,
        borderDash: [6, 4],
      })
    }

    chartRef.current?.destroy()
    chartRef.current = new Chart<'line', number[], string>(canvas, {
      type: 'line',
      data: { labels, datasets },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        // 薬事法対応: 凡例・ツールチップは値と日付のみで、判定文言を含まない
        plugins: {
          legend: { display: false },
        },
        scales: {
          y: { beginAtZero: false },
        },
      },
    })

    return () => {
      chartRef.current?.destroy()
      chartRef.current = null
    }
  }, [itemName, labels, values, unit, referenceMin, referenceMax])

  return (
    <div className="trend-chart-wrap">
      <canvas ref={canvasRef} role="img" aria-label={`${itemName}の経年グラフ`} />
    </div>
  )
}
