package com.rokusoudo.healthcheckup

import java.io.IOException

/**
 * Issue #17: ファイルからのOCR取り込み（PDF・画像）に関する MIME 判定・
 * エラー分類ロジック。Android フレームワークに依存しない純粋なロジックとして切り出し、
 * 単体テストできるようにする。
 */
object FileImportUtils {

    /** ファイル取り込み時のエラー種別。薬事法対応: 医療アドバイスは含まない。 */
    enum class FileImportError {
        /** application/pdf でも image 系（image/ 始まり）でもない形式 */
        UNSUPPORTED_FORMAT,

        /** パスワード付きPDF（[android.graphics.pdf.PdfRenderer] は非対応） */
        PASSWORD_PROTECTED,

        /** ファイルが壊れている・読み込めない */
        CORRUPTED_FILE,

        /** PDFにページが1つも含まれない */
        EMPTY_PDF,

        /** 上記以外の予期しないエラー */
        UNKNOWN
    }

    private const val MIME_PDF = "application/pdf"
    private const val MIME_IMAGE_PREFIX = "image/"

    fun isPdfMimeType(mimeType: String?): Boolean = mimeType == MIME_PDF

    fun isImageMimeType(mimeType: String?): Boolean =
        mimeType != null && mimeType.startsWith(MIME_IMAGE_PREFIX)

    /** [ActivityResultContracts.OpenDocument] で選択可能にした2種別（PDF / 画像）か判定する。 */
    fun isSupportedMimeType(mimeType: String?): Boolean =
        isPdfMimeType(mimeType) || isImageMimeType(mimeType)

    /**
     * ファイルの読み込み・PDFレンダリング中に発生した例外を、UIに表示するための
     * [FileImportError] に分類する。
     *
     * [android.graphics.pdf.PdfRenderer] はパスワード付きPDFに対して [SecurityException] を、
     * 破損ファイルに対しては [IOException] を投げる（Android公式ドキュメント準拠）。
     */
    fun classifyThrowable(throwable: Throwable): FileImportError = when (throwable) {
        is SecurityException -> FileImportError.PASSWORD_PROTECTED
        is IOException -> FileImportError.CORRUPTED_FILE
        is IllegalArgumentException -> FileImportError.CORRUPTED_FILE
        is IllegalStateException -> FileImportError.CORRUPTED_FILE
        else -> FileImportError.UNKNOWN
    }
}

/** ファイル取り込み処理中に発生したエラーを、分類済みの [FileImportUtils.FileImportError] とともに表す例外。 */
class FileImportException(
    val error: FileImportUtils.FileImportError,
    cause: Throwable? = null
) : Exception(cause)
