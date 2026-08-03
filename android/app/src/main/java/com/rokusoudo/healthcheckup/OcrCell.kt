package com.rokusoudo.healthcheckup

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * ML Kit の `Text.TextBlock` → `Line` → `Element` 相当の
 * 「文字列 + boundingBox（座標）」を表すセル。
 *
 * [OcrParser] の入力単位。CameraFragment → OcrResultFragment 間の
 * Navigation（Safe Args）引数のカスタム配列型としても使用するため、
 * `@Parcelize` で Parcelable を実装する（Safe Args のカスタム配列型は Parcelable 必須）。
 *
 * @param text   認識文字列（ML Kit の `Element.text` 相当）
 * @param left   boundingBox の left（px）
 * @param top    boundingBox の top（px）
 * @param right  boundingBox の right（px）
 * @param bottom boundingBox の bottom（px）
 * @param page   複数枚撮影時にどの画像（ページ）由来のセルかを表すインデックス（0始まり）。
 *               座標は画像ごとに独立した座標系のため、行・列クラスタリングは
 *               ページ単位で行い、ページをまたいで座標を混在させない。
 */
@Parcelize
data class OcrCell(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val page: Int = 0
) : Parcelable {

    /** boundingBox の高さ（px） */
    val height: Int get() = bottom - top

    /** boundingBox のY方向中心座標（px）。行クラスタリングに使用する。 */
    val centerY: Int get() = (top + bottom) / 2
}
