package com.rokusoudo.healthcheckup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import android.os.Build
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * Issue #16: 紙面の回転補正（再認識）のために、CameraX の [ImageProxy]（YUV_420_888）を
 * [Bitmap] に変換し、任意角度回転させるユーティリティ。
 *
 * 通常の1回目の認識は従来どおり `InputImage.fromMediaImage` を使用し（Bitmap変換のオーバーヘッドを
 * かけない）、[OcrRotationCorrector.shouldAttemptCorrection] が true を返した場合の
 * 再認識（最大1回）でのみ本ユーティリティを使用する。
 */
object ImageRotationUtils {

    /**
     * YUV_420_888 形式の [ImageProxy] を JPEG 経由で [Bitmap] に変換する。
     * OCR再認識用途の簡易変換であり、画質を厳密に保証するものではない。
     *
     * @throws IllegalStateException imageProxy.image が null の場合
     */
    fun toBitmap(imageProxy: ImageProxy): Bitmap {
        val image = imageProxy.image ?: throw IllegalStateException("ImageProxy.image is null")

        // 1回目の認識（InputImage.fromMediaImage）が内部でこれらのバッファを読み取っている
        // 可能性があるため、position に依存しないよう duplicate() + rewind() してから読む。
        val yBuffer = image.planes[0].buffer.duplicate().apply { rewind() }
        val uBuffer = image.planes[1].buffer.duplicate().apply { rewind() }
        val vBuffer = image.planes[2].buffer.duplicate().apply { rewind() }

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), JPEG_QUALITY, out)
        val jpegBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }

    /**
     * 画像ファイルの [Uri] を [Bitmap] にデコードする。回転補正の再認識用。
     *
     * API 28+ では ImageDecoder を使い、EXIFの向き補正を反映した状態でデコードする
     * （1回目の認識に使う `InputImage.fromFilePath` と同じ向きになる）。
     * API 26-27 では BitmapFactory によるデコードのためEXIFの向きは反映されないが、
     * その場合も再認識結果は [OcrRotationCorrector.shouldAdoptRotated] で元の結果と
     * 比較されるため、補正が効かないだけで結果が悪化することはない。
     */
    fun decodeBitmap(context: Context, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                // ML Kitに渡すため、HARDWAREではなくソフトウェアBitmapとしてデコードする
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            context.contentResolver.openInputStream(uri).use { stream ->
                BitmapFactory.decodeStream(stream)
                    ?: throw IllegalStateException("画像のデコードに失敗しました: $uri")
            }
        }
    }

    /**
     * [Bitmap] を指定角度（度、時計回り）だけ回転させた新しい [Bitmap] を返す。
     * degrees が 0（360の倍数含む）の場合は元の bitmap をそのまま返す。
     */
    fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        val normalized = ((degrees % 360) + 360) % 360
        if (normalized == 0) return bitmap

        val matrix = Matrix().apply { postRotate(normalized.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private const val JPEG_QUALITY = 90
}
