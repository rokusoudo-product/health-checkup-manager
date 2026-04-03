package com.rokusodo.healthcheckup

/**
 * OCR解析結果の品質を評価し、エラー種別を判定するクラス。
 * 薬事法対応: 医療アドバイスや改善提案は一切含まない。
 */
object OcrAnalyzer {

    /** OCR品質エラーの種別 */
    enum class OcrError {
        /** テキストが極端に少ない（読み取り失敗） */
        INSUFFICIENT_TEXT,

        /** 認識テキストの信頼度が低い（撮影品質の問題） */
        LOW_CONFIDENCE,

        /** エラーなし（品質OK） */
        NONE
    }

    /**
     * OCR結果テキストを評価し、エラー種別を返す。
     *
     * @param detectedText ML Kit が抽出したテキスト全体
     * @param blockCount   検出されたテキストブロック数（ML Kit TextRecognition.TextBlock）
     * @param avgConfidence 検出ブロックの平均信頼度（0.0〜1.0）。ML Kitでは直接取得不可なため
     *                      呼び出し側でヒューリスティックに計算して渡す
     * @return OcrError
     */
    fun evaluate(
        detectedText: String,
        blockCount: Int,
        avgConfidence: Float = 1.0f
    ): OcrError {
        // テキスト量が極端に少ない場合: 5文字未満 または ブロックが0
        if (detectedText.trim().length < MINIMUM_TEXT_LENGTH || blockCount == 0) {
            return OcrError.INSUFFICIENT_TEXT
        }

        // 信頼度が閾値未満の場合（呼び出し側から渡された値で判定）
        if (avgConfidence < CONFIDENCE_THRESHOLD) {
            return OcrError.LOW_CONFIDENCE
        }

        return OcrError.NONE
    }

    /**
     * ML Kit の Text オブジェクトから信頼度スコアをヒューリスティックで推定する。
     * ML Kit v2 ではブロック単位の confidence は公開されていないため、
     * 検出文字数とブロック密度から近似値を算出する。
     *
     * @param detectedText 全検出テキスト
     * @param blockCount ブロック数
     * @param lineCount ライン数
     * @return 0.0〜1.0 の推定信頼度
     */
    fun estimateConfidence(
        detectedText: String,
        blockCount: Int,
        lineCount: Int
    ): Float {
        if (blockCount == 0 || lineCount == 0) return 0.0f

        // 1ブロックあたりの平均文字数が少なすぎる場合は低信頼度とみなす
        val avgCharsPerLine = if (lineCount > 0) {
            detectedText.trim().length.toFloat() / lineCount
        } else {
            0f
        }

        return when {
            avgCharsPerLine >= MIN_CHARS_PER_LINE_HIGH_CONFIDENCE -> HIGH_CONFIDENCE_SCORE
            avgCharsPerLine >= MIN_CHARS_PER_LINE_MED_CONFIDENCE -> MED_CONFIDENCE_SCORE
            else -> LOW_CONFIDENCE_SCORE
        }
    }

    private const val MINIMUM_TEXT_LENGTH = 5
    private const val CONFIDENCE_THRESHOLD = 0.4f
    private const val MIN_CHARS_PER_LINE_HIGH_CONFIDENCE = 4.0f
    private const val MIN_CHARS_PER_LINE_MED_CONFIDENCE = 2.0f
    private const val HIGH_CONFIDENCE_SCORE = 0.85f
    private const val MED_CONFIDENCE_SCORE = 0.55f
    private const val LOW_CONFIDENCE_SCORE = 0.25f
}
