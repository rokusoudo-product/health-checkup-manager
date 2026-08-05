package com.rokusoudo.healthcheckup

import com.rokusoudo.healthcheckup.OcrRotationCorrector.CornerPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [OcrRotationCorrector] のテスト（Issue #16: 傾き・90度回転したスキャン画像の回転補正）。
 *
 * cornerPoints からの傾き角推定、90度単位への量子化、再認識トリガー判定、
 * 回転前後の採否判定の各ロジックを検証する。
 */
class OcrRotationCorrectorTest {

    // ------------------------------------------------------------
    // elementAngleDegrees
    // ------------------------------------------------------------

    @Test
    fun `水平な上辺ベクトルは0度に近い角度を返す`() {
        // 左上→右上が右方向を向く（通常の横書きテキスト）
        val corners = listOf(
            CornerPoint(0f, 0f),
            CornerPoint(100f, 0f),
            CornerPoint(100f, 20f),
            CornerPoint(0f, 20f)
        )
        val angle = OcrRotationCorrector.elementAngleDegrees(corners)
        assertEquals(0f, angle!!, 0.01f)
    }

    @Test
    fun `90度回転したテキストは約90度の角度を返す`() {
        // 左上→右上が下方向を向く（紙面が90度回転して撮影された場合）
        val corners = listOf(
            CornerPoint(0f, 0f),
            CornerPoint(0f, 100f),
            CornerPoint(-20f, 100f),
            CornerPoint(-20f, 0f)
        )
        val angle = OcrRotationCorrector.elementAngleDegrees(corners)
        assertEquals(90f, angle!!, 0.01f)
    }

    @Test
    fun `270度回転したテキストは約マイナス90度の角度を返す`() {
        val corners = listOf(
            CornerPoint(0f, 0f),
            CornerPoint(0f, -100f),
            CornerPoint(20f, -100f),
            CornerPoint(20f, 0f)
        )
        val angle = OcrRotationCorrector.elementAngleDegrees(corners)
        assertEquals(-90f, angle!!, 0.01f)
    }

    @Test
    fun `cornerPointsが2点未満の場合はnullを返す`() {
        assertNull(OcrRotationCorrector.elementAngleDegrees(listOf(CornerPoint(0f, 0f))))
        assertNull(OcrRotationCorrector.elementAngleDegrees(emptyList()))
    }

    @Test
    fun `始点と終点が同一点の場合はnullを返す`() {
        val corners = listOf(CornerPoint(5f, 5f), CornerPoint(5f, 5f))
        assertNull(OcrRotationCorrector.elementAngleDegrees(corners))
    }

    // ------------------------------------------------------------
    // medianAngleDegrees
    // ------------------------------------------------------------

    @Test
    fun `複数角度の中央値を返す(奇数個)`() {
        val median = OcrRotationCorrector.medianAngleDegrees(listOf(1f, 5f, 3f))
        assertEquals(3f, median!!, 0.01f)
    }

    @Test
    fun `複数角度の中央値を返す(偶数個)`() {
        val median = OcrRotationCorrector.medianAngleDegrees(listOf(1f, 3f, 5f, 7f))
        assertEquals(4f, median!!, 0.01f)
    }

    @Test
    fun `空リストの場合はnullを返す`() {
        assertNull(OcrRotationCorrector.medianAngleDegrees(emptyList()))
    }

    @Test
    fun `多少のノイズがあっても中央値で外れ値の影響を受けにくい`() {
        // ほぼ0度に揃っているが1要素だけ大きく外れた誤検出がある場合
        val angles = listOf(-2f, -1f, 0f, 1f, 2f, 88f)
        val median = OcrRotationCorrector.medianAngleDegrees(angles)
        assertEquals(0.5f, median!!, 0.01f)
    }

    // ------------------------------------------------------------
    // quantizeTo90
    // ------------------------------------------------------------

    @Test
    fun `中央値がnullの場合は0を返す`() {
        assertEquals(0, OcrRotationCorrector.quantizeTo90(null))
    }

    @Test
    fun `傾きが閾値未満なら0を返す(回転なし)`() {
        assertEquals(0, OcrRotationCorrector.quantizeTo90(0f))
        assertEquals(0, OcrRotationCorrector.quantizeTo90(5f))
        assertEquals(0, OcrRotationCorrector.quantizeTo90(-5f))
        assertEquals(0, OcrRotationCorrector.quantizeTo90(19.9f))
    }

    @Test
    fun `約90度の傾きは90を返す`() {
        assertEquals(90, OcrRotationCorrector.quantizeTo90(90f))
        assertEquals(90, OcrRotationCorrector.quantizeTo90(85f))
        assertEquals(90, OcrRotationCorrector.quantizeTo90(100f))
    }

    @Test
    fun `約マイナス90度(270度)の傾きは270を返す`() {
        assertEquals(270, OcrRotationCorrector.quantizeTo90(-90f))
        assertEquals(270, OcrRotationCorrector.quantizeTo90(-85f))
        assertEquals(270, OcrRotationCorrector.quantizeTo90(-100f))
    }

    @Test
    fun `180度付近の傾きはcornerPointsからは区別できないため0を返す`() {
        assertEquals(0, OcrRotationCorrector.quantizeTo90(180f))
        assertEquals(0, OcrRotationCorrector.quantizeTo90(-180f))
        assertEquals(0, OcrRotationCorrector.quantizeTo90(175f))
        assertEquals(0, OcrRotationCorrector.quantizeTo90(-175f))
    }

    @Test
    fun `閾値付近の境界を確認する`() {
        // threshold=20の場合、20度ちょうどは90側に含まれる
        assertEquals(90, OcrRotationCorrector.quantizeTo90(20f))
        assertEquals(0, OcrRotationCorrector.quantizeTo90(19.99f))
    }

    // ------------------------------------------------------------
    // shouldAttemptCorrection
    // ------------------------------------------------------------

    @Test
    fun `90または270が検出された場合は常に再認識を試みる`() {
        assertTrue(OcrRotationCorrector.shouldAttemptCorrection(90, elementCount = 50, charCount = 200))
        assertTrue(OcrRotationCorrector.shouldAttemptCorrection(270, elementCount = 50, charCount = 200))
    }

    @Test
    fun `回転が検出されず認識品質が十分な場合は再認識しない`() {
        assertFalse(
            OcrRotationCorrector.shouldAttemptCorrection(0, elementCount = 20, charCount = 100)
        )
    }

    @Test
    fun `回転が検出されないが要素数が極端に少ない場合は180度回転を疑い再認識する`() {
        assertTrue(
            OcrRotationCorrector.shouldAttemptCorrection(0, elementCount = 1, charCount = 100)
        )
    }

    @Test
    fun `回転が検出されないが文字数が極端に少ない場合は180度回転を疑い再認識する`() {
        assertTrue(
            OcrRotationCorrector.shouldAttemptCorrection(0, elementCount = 20, charCount = 2)
        )
    }

    // ------------------------------------------------------------
    // resolveCandidateDegrees
    // ------------------------------------------------------------

    @Test
    fun `回転候補が0の場合は180度を候補として返す`() {
        assertEquals(180, OcrRotationCorrector.resolveCandidateDegrees(0))
    }

    @Test
    fun `回転候補が90または270の場合はそのまま返す`() {
        assertEquals(90, OcrRotationCorrector.resolveCandidateDegrees(90))
        assertEquals(270, OcrRotationCorrector.resolveCandidateDegrees(270))
    }

    // ------------------------------------------------------------
    // shouldAdoptRotated
    // ------------------------------------------------------------

    @Test
    fun `回転後の文字数が多い場合は回転後を採用する`() {
        assertTrue(
            OcrRotationCorrector.shouldAdoptRotated(
                originalCharCount = 5,
                originalLineCount = 1,
                rotatedCharCount = 120,
                rotatedLineCount = 15
            )
        )
    }

    @Test
    fun `回転後の文字数が少ない場合は元の結果を採用する`() {
        assertFalse(
            OcrRotationCorrector.shouldAdoptRotated(
                originalCharCount = 120,
                originalLineCount = 15,
                rotatedCharCount = 5,
                rotatedLineCount = 1
            )
        )
    }

    @Test
    fun `文字数が同数の場合は行数で判定する`() {
        assertTrue(
            OcrRotationCorrector.shouldAdoptRotated(
                originalCharCount = 100,
                originalLineCount = 5,
                rotatedCharCount = 100,
                rotatedLineCount = 10
            )
        )
        assertFalse(
            OcrRotationCorrector.shouldAdoptRotated(
                originalCharCount = 100,
                originalLineCount = 10,
                rotatedCharCount = 100,
                rotatedLineCount = 5
            )
        )
    }

    @Test
    fun `文字数・行数とも完全に同一の場合は元の結果を優先し不要な回転採用を避ける`() {
        assertFalse(
            OcrRotationCorrector.shouldAdoptRotated(
                originalCharCount = 100,
                originalLineCount = 10,
                rotatedCharCount = 100,
                rotatedLineCount = 10
            )
        )
    }

    // ------------------------------------------------------------
    // 統合的なシナリオ（90度回転したページを想定した一連の流れ）
    // ------------------------------------------------------------

    @Test
    fun `90度回転したページを想定した一連の判定フローを確認する`() {
        // 複数要素がすべて約90度傾いている（多少のノイズを含む）
        val angles = listOf(88f, 90f, 91f, 89f, 92f)
        val median = OcrRotationCorrector.medianAngleDegrees(angles)
        val candidate = OcrRotationCorrector.quantizeTo90(median)
        assertEquals(90, candidate)

        assertTrue(
            OcrRotationCorrector.shouldAttemptCorrection(
                rotationCandidate = candidate,
                elementCount = angles.size,
                charCount = 30
            )
        )
        assertEquals(90, OcrRotationCorrector.resolveCandidateDegrees(candidate))
    }

    // ------------------------------------------------------------
    // 単体テスト上でのベンチマーク（Issue #16注意事項:
    // 実機計測が困難な場合、回転判定ロジックの実行時間で代替してよい）
    // ------------------------------------------------------------

    @Test
    fun `回転判定ロジック(中央値計算～量子化)は十分高速である`() {
        val angles = (0 until 500).map { (it % 7 - 3).toFloat() } // 0度近傍のノイズを想定
        val start = System.nanoTime()
        repeat(1000) {
            val median = OcrRotationCorrector.medianAngleDegrees(angles)
            OcrRotationCorrector.quantizeTo90(median)
        }
        val elapsedMillis = (System.nanoTime() - start) / 1_000_000.0
        // 1000回繰り返しても100ms未満であれば、1回あたりのコストは実用上無視できる
        assertTrue("回転判定ロジックが想定より遅い: ${elapsedMillis}ms", elapsedMillis < 100.0)
    }
}
