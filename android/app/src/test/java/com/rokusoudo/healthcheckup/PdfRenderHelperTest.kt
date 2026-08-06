package com.rokusoudo.healthcheckup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Issue #17: PdfRenderHelper のテスト。
 * PdfRenderer 自体には依存しない純粋なロジック（レンダリング解像度・ページ数の計算）を検証する。
 */
class PdfRenderHelperTest {

    @Test
    fun `A4サイズのページを300dpiでレンダリングすると想定どおりのpx数になる`() {
        // A4 = 595 x 842 pt
        val (width, height) = PdfRenderHelper.renderSizeFor(595, 842, targetDpi = 300)
        // 595 / 72 * 300 = 2479.16... -> 2479
        assertEquals(2479, width)
        // 842 / 72 * 300 = 3508.33... -> 3508
        assertEquals(3508, height)
    }

    @Test
    fun `US Letterサイズのページを300dpiでレンダリングすると想定どおりのpx数になる`() {
        // US Letter = 612 x 792 pt
        val (width, height) = PdfRenderHelper.renderSizeFor(612, 792, targetDpi = 300)
        assertEquals(2550, width)
        assertEquals(3300, height)
    }

    @Test
    fun `極端に大きいページサイズはMAX_DIMENSION_PXを超えないようアスペクト比を保って縮小される`() {
        // 巨大なポスターサイズ想定 (2000pt x 1000pt) を高DPIでレンダリングすると
        // 素の計算では上限を大きく超えるため、縮小されることを確認する。
        val (width, height) = PdfRenderHelper.renderSizeFor(2000, 1000, targetDpi = 300)

        assertTrue("width must not exceed MAX_DIMENSION_PX", width <= PdfRenderHelper.MAX_DIMENSION_PX)
        assertTrue("height must not exceed MAX_DIMENSION_PX", height <= PdfRenderHelper.MAX_DIMENSION_PX)
        // アスペクト比（2:1）が維持されていること
        assertEquals(2.0, width.toDouble() / height.toDouble(), 0.01)
        // 長辺がちょうど上限に張り付いていること
        assertEquals(PdfRenderHelper.MAX_DIMENSION_PX, maxOf(width, height))
    }

    @Test
    fun `ページサイズが0以下の場合は例外を投げる`() {
        assertThrows(IllegalArgumentException::class.java) {
            PdfRenderHelper.renderSizeFor(0, 842)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PdfRenderHelper.renderSizeFor(595, -1)
        }
    }

    @Test
    fun `ページ数が上限以下の場合はそのまま返す`() {
        assertEquals(1, PdfRenderHelper.clampPageCount(1))
        assertEquals(PdfRenderHelper.MAX_PAGE_COUNT, PdfRenderHelper.clampPageCount(PdfRenderHelper.MAX_PAGE_COUNT))
    }

    @Test
    fun `ページ数が上限を超える場合は上限に切り詰められる`() {
        assertEquals(PdfRenderHelper.MAX_PAGE_COUNT, PdfRenderHelper.clampPageCount(PdfRenderHelper.MAX_PAGE_COUNT + 50))
    }

    @Test
    fun `ページ数が0の場合は0を返す`() {
        assertEquals(0, PdfRenderHelper.clampPageCount(0))
    }
}
