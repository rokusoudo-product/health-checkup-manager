package com.rokusoudo.healthcheckup

import kotlin.math.roundToInt

/**
 * Issue #17: PDFページを OCR にかけられる Bitmap にレンダリングする際の
 * 解像度計算ロジック。[android.graphics.pdf.PdfRenderer] 自体を使わない
 * 純粋なロジックとして切り出し、Robolectric 等の実機依存なしに単体テストできるようにする。
 */
object PdfRenderHelper {

    /**
     * OCR精度を確保するための目標解像度（dpi）。
     * PDFページのサイズは pt（1/72インチ）単位のため、300dpi前後（一般的なスキャナ相当）を
     * 基準に拡大してレンダリングしないと、健診表の細かい数値をML Kitが誤認識しやすくなる。
     */
    const val TARGET_DPI = 300

    /**
     * レンダリングするビットマップの1辺あたりの上限（px）。
     * 極端に大きいページサイズ（例: ポスターサイズのPDF）をそのまま300dpiで
     * レンダリングするとOOMの危険があるため、上限を超える場合はアスペクト比を保って縮小する。
     */
    const val MAX_DIMENSION_PX = 4000

    /**
     * 複数ページPDFで処理するページ数の上限。
     * 上限なく全ページを処理すると、極端に多いページ数のPDFでOOM・処理時間の
     * 増大を招くため上限を設ける（受け入れ基準の「複数ページのPDFが扱える」は
     * 通常の健診結果PDF（数ページ程度）を想定しており、この上限で十分にカバーできる）。
     */
    const val MAX_PAGE_COUNT = 20

    private const val POINTS_PER_INCH = 72f

    /**
     * PDFページのサイズ（pt。[android.graphics.pdf.PdfRenderer.Page.getWidth] /
     * [android.graphics.pdf.PdfRenderer.Page.getHeight] と同じ単位）から、
     * [targetDpi] を基準にレンダリングすべきビットマップの幅・高さ（px）を計算する。
     *
     * @throws IllegalArgumentException pageWidthPoints / pageHeightPoints が0以下の場合
     */
    fun renderSizeFor(
        pageWidthPoints: Int,
        pageHeightPoints: Int,
        targetDpi: Int = TARGET_DPI
    ): Pair<Int, Int> {
        require(pageWidthPoints > 0 && pageHeightPoints > 0) {
            "PDFページサイズは正の値である必要があります: width=$pageWidthPoints, height=$pageHeightPoints"
        }

        val scale = targetDpi / POINTS_PER_INCH
        var width = (pageWidthPoints * scale).roundToInt()
        var height = (pageHeightPoints * scale).roundToInt()

        val largerSide = maxOf(width, height)
        if (largerSide > MAX_DIMENSION_PX) {
            val shrink = MAX_DIMENSION_PX.toFloat() / largerSide
            width = (width * shrink).roundToInt()
            height = (height * shrink).roundToInt()
        }

        return width.coerceAtLeast(1) to height.coerceAtLeast(1)
    }

    /**
     * PDFの総ページ数のうち、実際に処理するページ数を [MAX_PAGE_COUNT] で切り詰めて返す。
     */
    fun clampPageCount(totalPageCount: Int): Int =
        totalPageCount.coerceIn(0, MAX_PAGE_COUNT)
}
