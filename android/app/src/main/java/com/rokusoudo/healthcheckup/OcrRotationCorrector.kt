package com.rokusoudo.healthcheckup

import kotlin.math.atan2

/**
 * スキャン画像内の紙面（健診表）自体の傾き・90度単位の回転を、
 * ML Kit の認識結果（`Text.Element.cornerPoints`）から推定し、
 * 補正すべき回転量を判定するロジック。
 *
 * ## 背景（Issue #16）
 * `CameraFragment` は ML Kit へ `imageProxy.imageInfo.rotationDegrees`（カメラの向き）を
 * 渡しているだけで、紙面自体が回転して撮影された場合の補正が存在しない。
 * 表が回転していると、座標ベースのレイアウト解析（Issue #12 の [OcrParser]）が
 * 行・列を取り違え、解析が成立しない。
 *
 * ## アルゴリズム
 * 1. 一度 ML Kit で認識し、各要素の `cornerPoints`（4点）から上辺ベクトルの傾き角を算出する（[elementAngleDegrees]）
 * 2. 全要素の傾き角の中央値を紙面の回転角とみなす（[medianAngleDegrees]）
 * 3. 中央値から 0 / 90 / 270 度のいずれかを判定する（[quantizeTo90]）。
 *    180度回転は、紙面上のテキストボックス自体の矩形の傾きは0度回転時とほぼ変わらず
 *    （矩形の向きは変わらず中身の文字だけが天地反転するため）cornerPoints からは
 *    区別できない。そのため本メソッドは 180 を返さず、0（=90/270ほど明確でない）として扱う。
 * 4. [shouldAttemptCorrection] で「90/270が明確に検出された」または
 *    「傾きは検出できないが認識品質が低く180度回転の疑いがある」場合にのみ
 *    再認識を行うと判定する（回転なしの正常な画像では再認識を行わず、処理時間を増やさない）。
 * 5. 再認識を行う場合の回転候補は [resolveCandidateDegrees] で決定する
 *    （90/270が検出されていればそれを、検出されていなければ180を候補とする）。
 * 6. 再認識（1回のみ）の結果と元の結果を [shouldAdoptRotated] で比較し、
 *    認識できた文字数（同数の場合は行数）が多い方を採用する。
 *
 * 呼び出し側（[CameraFragment]）は、実際の画像回転・ML Kit 再実行（Android/ML Kit依存の処理）を担当し、
 * このオブジェクトは純粋なロジック（角度計算・閾値判定）のみを担当する。単体テストしやすくするため
 * Android フレームワークの型（`android.graphics.Point` 等）には依存しない。
 */
object OcrRotationCorrector {

    /** cornerPoints の1点を表す軽量な座標（Android非依存、テスト容易化のため独自定義）。 */
    data class CornerPoint(val x: Float, val y: Float)

    // 傾き角の判定閾値（度）。この値未満・または180度近傍（180-この値超）は
    // 「90/270としては明確でない」とみなす。
    private const val DEFAULT_THRESHOLD_DEGREES = 20f

    // shouldAttemptCorrection: 明確な回転が検出されない場合に「180度回転の疑いあり」と
    // みなす認識品質の閾値。
    private const val MIN_ELEMENTS_FOR_CONFIDENT_UPRIGHT = 3
    private const val MIN_CHARS_FOR_CONFIDENT_UPRIGHT = 10

    /**
     * 要素の cornerPoints（ML Kit 仕様: 左上→右上→右下→左下の順の4点）から、
     * 上辺ベクトル（corners[0]→corners[1]）の傾き角を度数で返す。
     * 画像座標系（Y軸下向きが正）での atan2 を用いるため、返る範囲は (-180, 180]。
     *
     * @return corners が2点未満、または始点・終点が同一点の場合は null
     */
    fun elementAngleDegrees(corners: List<CornerPoint>): Float? {
        if (corners.size < 2) return null
        val dx = corners[1].x - corners[0].x
        val dy = corners[1].y - corners[0].y
        if (dx == 0f && dy == 0f) return null
        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    }

    /**
     * 複数要素の傾き角（[elementAngleDegrees] の結果群）の中央値を返す。
     *
     * @return angles が空の場合は null
     */
    fun medianAngleDegrees(angles: List<Float>): Float? {
        if (angles.isEmpty()) return null
        val sorted = angles.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2f
        } else {
            sorted[mid]
        }
    }

    /**
     * 傾き角の中央値から、90度単位の回転量を判定する。
     *
     * @param medianAngle [medianAngleDegrees] の結果
     * @param thresholdDegrees 90/270と判定する角度の許容幅（度）
     * @return 0 / 90 / 270 のいずれか。180 は返さない（上記クラスコメント参照）。
     *         medianAngle が null の場合は 0。
     */
    fun quantizeTo90(
        medianAngle: Float?,
        thresholdDegrees: Float = DEFAULT_THRESHOLD_DEGREES
    ): Int {
        if (medianAngle == null) return 0

        var normalized = medianAngle % 360f
        if (normalized > 180f) normalized -= 360f
        if (normalized <= -180f) normalized += 360f

        return when {
            normalized in thresholdDegrees..(180f - thresholdDegrees) -> 90
            normalized in (-(180f - thresholdDegrees))..(-thresholdDegrees) -> 270
            else -> 0
        }
    }

    /**
     * 再認識（1回限り）を試みるべきかどうかを判定する。
     *
     * - 90/270 が明確に検出された場合（[quantizeTo90] が0以外）は常に試みる
     * - 明確な回転は検出されなかったが、認識できた要素数・文字数が極端に少ない場合、
     *   180度回転の疑いがあるとみなして試みる
     *
     * @param rotationCandidate [quantizeTo90] の結果
     * @param elementCount 1回目の認識で得られた要素（cornerPointsを持つ文字断片）数
     * @param charCount 1回目の認識で得られた総文字数
     */
    fun shouldAttemptCorrection(
        rotationCandidate: Int,
        elementCount: Int,
        charCount: Int
    ): Boolean {
        if (rotationCandidate != 0) return true
        return elementCount < MIN_ELEMENTS_FOR_CONFIDENT_UPRIGHT ||
            charCount < MIN_CHARS_FOR_CONFIDENT_UPRIGHT
    }

    /**
     * 再認識で試すべき回転量（度）を決定する。
     * 90/270 が明確に検出されていればそれを、検出されていなければ
     * （[shouldAttemptCorrection] が低品質を理由にtrueを返したケース）180を候補とする。
     */
    fun resolveCandidateDegrees(rotationCandidate: Int): Int =
        if (rotationCandidate == 0) 180 else rotationCandidate

    /**
     * 回転前後の認識結果を比較し、回転後を採用すべきか判定する。
     * 認識できた文字数が多い方を優先し、同数の場合は行数が多い方を採用する。
     * 完全に同じ場合は元の結果（回転なし）を優先する（不要な回転を避ける）。
     */
    fun shouldAdoptRotated(
        originalCharCount: Int,
        originalLineCount: Int,
        rotatedCharCount: Int,
        rotatedLineCount: Int
    ): Boolean {
        if (rotatedCharCount != originalCharCount) {
            return rotatedCharCount > originalCharCount
        }
        return rotatedLineCount > originalLineCount
    }
}
