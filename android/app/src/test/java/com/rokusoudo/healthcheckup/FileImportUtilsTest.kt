package com.rokusoudo.healthcheckup

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Issue #17: FileImportUtils のテスト。
 * SAFで選択したファイルのMIME判定と、PdfRenderer等が投げる例外の分類ロジックを検証する。
 */
class FileImportUtilsTest {

    // ------------------------------------------------------------
    // MIMEタイプ判定
    // ------------------------------------------------------------

    @Test
    fun `application_pdfはPDFとして判定される`() {
        assertTrue(FileImportUtils.isPdfMimeType("application/pdf"))
        assertTrue(FileImportUtils.isSupportedMimeType("application/pdf"))
    }

    @Test
    fun `image_で始まるMIMEタイプは画像として判定される`() {
        assertTrue(FileImportUtils.isImageMimeType("image/jpeg"))
        assertTrue(FileImportUtils.isImageMimeType("image/png"))
        assertTrue(FileImportUtils.isSupportedMimeType("image/jpeg"))
    }

    @Test
    fun `PDFでも画像でもないMIMEタイプは非対応と判定される`() {
        assertFalse(FileImportUtils.isSupportedMimeType("text/plain"))
        assertFalse(FileImportUtils.isSupportedMimeType("application/zip"))
        assertFalse(FileImportUtils.isPdfMimeType("image/jpeg"))
        assertFalse(FileImportUtils.isImageMimeType("application/pdf"))
    }

    @Test
    fun `MIMEタイプがnullの場合は非対応と判定される`() {
        assertFalse(FileImportUtils.isSupportedMimeType(null))
        assertFalse(FileImportUtils.isPdfMimeType(null))
        assertFalse(FileImportUtils.isImageMimeType(null))
    }

    // ------------------------------------------------------------
    // 例外分類（PdfRenderer等が投げる例外 → ユーザー提示用エラー種別）
    // ------------------------------------------------------------

    @Test
    fun `SecurityExceptionはパスワード付きPDFとして分類される`() {
        assertEquals(
            FileImportUtils.FileImportError.PASSWORD_PROTECTED,
            FileImportUtils.classifyThrowable(SecurityException("password required"))
        )
    }

    @Test
    fun `IOExceptionは破損ファイルとして分類される`() {
        assertEquals(
            FileImportUtils.FileImportError.CORRUPTED_FILE,
            FileImportUtils.classifyThrowable(IOException("damaged pdf"))
        )
    }

    @Test
    fun `IllegalArgumentExceptionとIllegalStateExceptionは破損ファイルとして分類される`() {
        assertEquals(
            FileImportUtils.FileImportError.CORRUPTED_FILE,
            FileImportUtils.classifyThrowable(IllegalArgumentException("bad arg"))
        )
        assertEquals(
            FileImportUtils.FileImportError.CORRUPTED_FILE,
            FileImportUtils.classifyThrowable(IllegalStateException("bad state"))
        )
    }

    @Test
    fun `想定外の例外はUNKNOWNとして分類される`() {
        assertEquals(
            FileImportUtils.FileImportError.UNKNOWN,
            FileImportUtils.classifyThrowable(RuntimeException("something else"))
        )
    }

    // ------------------------------------------------------------
    // FileImportException
    // ------------------------------------------------------------

    @Test
    fun `FileImportExceptionはエラー種別と原因を保持する`() {
        val cause = IOException("damaged")
        val exception = FileImportException(FileImportUtils.FileImportError.CORRUPTED_FILE, cause)

        assertEquals(FileImportUtils.FileImportError.CORRUPTED_FILE, exception.error)
        assertEquals(cause, exception.cause)
    }
}
