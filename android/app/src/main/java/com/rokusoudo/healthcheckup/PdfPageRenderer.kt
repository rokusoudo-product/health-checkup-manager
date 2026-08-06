package com.rokusoudo.healthcheckup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor

/**
 * Issue #17: Storage Access Framework で選択したPDFの各ページを、OCRにかけられる
 * [Bitmap] にレンダリングする。
 *
 * 対象PDFはカメラでスキャンした画像PDFであり、テキストレイヤーを持たないため、
 * テキスト直接抽出は行わずページを画像化してML KitのOCRにかける方式を採る。
 *
 * 複数ページPDFは、ページ選択UIを設けず全ページを [PdfRenderHelper.MAX_PAGE_COUNT] まで
 * レンダリングして返す（呼び出し側でカメラ撮影と同様に全ページ分OCRし結果を統合する）。
 */
object PdfPageRenderer {

    /**
     * [uri] が指すPDFの各ページを [Bitmap] のリストとしてレンダリングする。
     * 呼び出し側でIOディスパッチャ上から呼ぶこと（同期I/O・レンダリングを含むため）。
     *
     * @throws FileImportException 破損ファイル・パスワード付きPDF・ページ0件など、
     *   ユーザーに提示すべきエラーが発生した場合
     */
    fun renderPages(context: Context, uri: Uri): List<Bitmap> {
        val descriptor: ParcelFileDescriptor = try {
            context.contentResolver.openFileDescriptor(uri, "r")
                ?: throw FileImportException(FileImportUtils.FileImportError.CORRUPTED_FILE)
        } catch (e: FileImportException) {
            throw e
        } catch (e: Exception) {
            throw FileImportException(FileImportUtils.classifyThrowable(e), e)
        }

        descriptor.use { pfd ->
            val renderer = try {
                PdfRenderer(pfd)
            } catch (e: Exception) {
                throw FileImportException(FileImportUtils.classifyThrowable(e), e)
            }

            renderer.use { r ->
                if (r.pageCount == 0) {
                    throw FileImportException(FileImportUtils.FileImportError.EMPTY_PDF)
                }

                val pageCount = PdfRenderHelper.clampPageCount(r.pageCount)
                return (0 until pageCount).map { index -> renderPage(r, index) }
            }
        }
    }

    private fun renderPage(renderer: PdfRenderer, index: Int): Bitmap {
        try {
            renderer.openPage(index).use { page ->
                val (width, height) = PdfRenderHelper.renderSizeFor(page.width, page.height)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                // スキャンPDFは通常不透明な白背景のため、透過ピクセルによる誤認識を避けて
                // 白で初期化してからレンダリングする。
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                return bitmap
            }
        } catch (e: FileImportException) {
            throw e
        } catch (e: Exception) {
            throw FileImportException(FileImportUtils.classifyThrowable(e), e)
        }
    }
}
