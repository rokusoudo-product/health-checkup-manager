package com.rokusoudo.healthcheckup

import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import java.text.Normalizer

/**
 * OCRで抽出した項目名を項目マスタ（[ItemMaster]）に正規化・照合するマッチャー（Issue #15）。
 *
 * ## 背景
 * [OcrParser] は座標クラスタリングでグリッドを復元し「項目名・値・単位」を機械的に切り出すが、
 * 項目マスタとの照合は行わない。そのため、OCRの誤認識や表の罫線・注記に由来する
 * 「22/7」「87.0」のような意味をなさない文字列がそのまま項目名として確認画面に流れ込む。
 * 本ファイルは、その最後の砦として項目マスタとの照合・正規化・除外を担う。
 *
 * [OcrParser] とは独立したファイル・独立した関数として実装する
 * （Issue #14 列選択の並行実装（PR #36）と同一ファイルを競合させないため）。
 *
 * ## アルゴリズム
 * 1. 項目名・マスタ項目名の双方を [normalize] で正規化する
 *    （全角/半角、カッコ種別、スペース有無、大文字/小文字の表記ゆれを吸収）
 * 2. 正規化後の完全一致を試みる
 * 3. 完全一致しない場合は編集距離ベースの類似度（[similarity]）によるあいまい一致を試みる。
 *    [FUZZY_SIMILARITY_THRESHOLD] 以上の類似度を持つマスタ項目のうち最も類似度が高いものを採用する
 *    （例: 「γ-GTP」に対する「γ-GT」のような表記ゆれ・脱字を吸収する）
 * 4. いずれにも該当しない行は除外する
 * 5. 一致した行は、項目名をマスタの正式名称に、単位をマスタ登録値に置き換える
 *    （OCRで読み取った単位が異なっていてもマスタの単位を優先する）
 *
 * 項目名・値の両方が空欄の行（[OcrParser] が抽出結果0件のときに返す手動入力用プレースホルダー等）は
 * 照合・除外の対象外とし、そのまま保持する。
 */
object ItemMasterMatcher {

    /**
     * [match] の結果。
     *
     * @param items         マスタに正規化された項目のリスト（除外された行は含まない）
     * @param excludedCount マスタに該当せず除外した行数
     */
    data class MatchResult(
        val items: List<OcrItem>,
        val excludedCount: Int
    )

    // あいまい一致の採用閾値。類似度がこの値を"超えた"マスタ項目のみ採用する。
    // 「γ-GTP」(5文字) と「γ-GT」(4文字、1文字欠落) の類似度が 0.8 になるよう実測した上で、
    // 無関係な短い文字列同士がたまたま高い類似度を持つケースを避けられる値として 0.75 を採用。
    private const val FUZZY_SIMILARITY_THRESHOLD = 0.75

    // カッコ種別の表記ゆれを吸収するため、正規化時に読み捨てる括弧文字。
    // 全角丸カッコ・全角角カッコは NFKC 正規化で半角に変換されるため、変換後の半角文字と
    // NFKC で変換されないCJK系の括弧（〔〕【】山括弧等）の両方を含める。
    private val BRACKET_CHARS = "()[]{}〔〕【】<>＜＞".toSet()

    /**
     * OCR抽出結果を項目マスタに照合する。
     *
     * @param items   [OcrParser] が抽出した [OcrItem] のリスト
     * @param masters 項目マスタの全件（[com.rokusoudo.healthcheckup.data.repository.HealthRepository.getAllMasters] 等で取得）
     * @return マスタに正規化された項目のリストと除外件数
     */
    fun match(items: List<OcrItem>, masters: List<ItemMaster>): MatchResult {
        val normalizedMasters = masters.map { it to normalize(it.itemName) }

        val matched = mutableListOf<OcrItem>()
        var excludedCount = 0

        for (item in items) {
            // 項目名・値の両方が空欄の行（手動入力用プレースホルダー等）は照合対象外
            if (item.itemName.isBlank() && item.value.isBlank()) {
                matched.add(item)
                continue
            }

            val master = findBestMaster(normalize(item.itemName), normalizedMasters)
            if (master != null) {
                matched.add(item.copy(itemName = master.itemName, unit = master.unit))
            } else {
                excludedCount++
            }
        }

        return MatchResult(items = matched, excludedCount = excludedCount)
    }

    private fun findBestMaster(
        normalizedName: String,
        normalizedMasters: List<Pair<ItemMaster, String>>
    ): ItemMaster? {
        if (normalizedName.isBlank()) return null

        // 1. 正規化後の完全一致を優先する
        normalizedMasters.firstOrNull { it.second == normalizedName }?.let { return it.first }

        // 2. あいまい一致: 閾値を超える類似度の中で最も高いものを採用する
        var bestMaster: ItemMaster? = null
        var bestSimilarity = FUZZY_SIMILARITY_THRESHOLD
        for ((master, normalizedMasterName) in normalizedMasters) {
            val score = similarity(normalizedName, normalizedMasterName)
            if (score > bestSimilarity) {
                bestSimilarity = score
                bestMaster = master
            }
        }
        return bestMaster
    }

    /**
     * 項目名の表記ゆれ（全角/半角、カッコ種別、スペース有無、大文字/小文字）を吸収した
     * 比較用の正規化文字列を返す。
     */
    internal fun normalize(text: String): String {
        val nfkc = Normalizer.normalize(text.trim(), Normalizer.Form.NFKC)
        return buildString {
            for (c in nfkc) {
                if (c.isWhitespace()) continue
                if (c in BRACKET_CHARS) continue
                append(c.lowercaseChar())
            }
        }
    }

    /**
     * 正規化済み文字列同士の類似度（0.0〜1.0）を、編集距離（レーベンシュタイン距離）から算出する。
     * 1.0 が完全一致、0.0 が共通点なし。
     */
    internal fun similarity(a: String, b: String): Double {
        if (a.isEmpty() && b.isEmpty()) return 1.0
        val maxLen = maxOf(a.length, b.length)
        if (maxLen == 0) return 1.0
        val distance = levenshtein(a, b)
        return 1.0 - distance.toDouble() / maxLen
    }

    /** 2文字列間のレーベンシュタイン距離（編集距離）を求める。 */
    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        var previousRow = IntArray(b.length + 1) { it }
        var currentRow = IntArray(b.length + 1)

        for (i in 1..a.length) {
            currentRow[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                currentRow[j] = minOf(
                    currentRow[j - 1] + 1,      // 挿入
                    previousRow[j] + 1,         // 削除
                    previousRow[j - 1] + cost   // 置換
                )
            }
            val tmp = previousRow
            previousRow = currentRow
            currentRow = tmp
        }
        return previousRow[b.length]
    }
}
