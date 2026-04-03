package com.rokusodo.healthcheckup

/**
 * OCR抽出テキストを行単位で解析し、「項目名・値・単位」のリストに変換するパーサー。
 *
 * 対応パターン:
 *   - `(\S+)\s+([\d.]+)\s*([a-zA-Z/%]+)?`
 *   例: "血圧(収縮期) 120 mmHg" → OcrItem("血圧(収縮期)", "120", "mmHg")
 *   例: "HbA1c 5.6 %" → OcrItem("HbA1c", "5.6", "%")
 *   例: "体重 68.5" → OcrItem("体重", "68.5", "")
 */
object OcrParser {

    // 項目名（非空白文字列）+ 数値（整数 or 小数）+ 任意の単位
    private val LINE_PATTERN = Regex("""(\S+)\s+([\d.]+)\s*([a-zA-Z/%]+)?""")

    /**
     * OCRテキスト全体を解析してOcrItemリストを返す。
     *
     * @param rawText ML Kit から抽出されたテキスト
     * @return 解析結果のリスト。パターンにマッチしない行は空のOcrItemとして追加しない。
     *         ただし、全行がマッチしなかった場合は空のOcrItem1件を返し手動入力を促す。
     */
    fun parse(rawText: String): List<OcrItem> {
        if (rawText.isBlank()) {
            return listOf(OcrItem("", "", ""))
        }

        val results = rawText.lines()
            .mapNotNull { line -> parseLine(line.trim()) }
            .filter { it.itemName.isNotBlank() || it.value.isNotBlank() }

        return results.ifEmpty { listOf(OcrItem("", "", "")) }
    }

    /**
     * 1行のテキストを解析してOcrItemに変換する。マッチしない場合はnullを返す。
     */
    private fun parseLine(line: String): OcrItem? {
        if (line.isBlank()) return null

        val match = LINE_PATTERN.find(line) ?: return null
        val (itemName, value, unit) = match.destructured
        return OcrItem(
            itemName = itemName,
            value = value,
            unit = unit
        )
    }
}

/**
 * OCR解析結果の1行分を表すデータクラス。
 *
 * @param itemName 項目名（例: "血圧(収縮期)"）
 * @param value    数値文字列（例: "120"）
 * @param unit     単位（例: "mmHg"）。未検出の場合は空文字列。
 */
data class OcrItem(
    val itemName: String,
    val value: String,
    val unit: String
)
