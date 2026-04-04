package com.rokusodo.healthcheckup;

/**
 * OCR抽出テキストを行単位で解析し、「項目名・値・単位」のリストに変換するパーサー。
 *
 * 対応パターン:
 *  - `(\S+)\s+([\d.]+)\s*([a-zA-Z/%]+)?`
 *  例: "血圧(収縮期) 120 mmHg" → OcrItem("血圧(収縮期)", "120", "mmHg")
 *  例: "HbA1c 5.6 %" → OcrItem("HbA1c", "5.6", "%")
 *  例: "体重 68.5" → OcrItem("体重", "68.5", "")
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\tJ\u0012\u0010\n\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u000b\u001a\u00020\tH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/rokusodo/healthcheckup/OcrParser;", "", "()V", "LINE_PATTERN", "Lkotlin/text/Regex;", "parse", "", "Lcom/rokusodo/healthcheckup/OcrItem;", "rawText", "", "parseLine", "line", "app_debug"})
public final class OcrParser {
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.text.Regex LINE_PATTERN = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.rokusodo.healthcheckup.OcrParser INSTANCE = null;
    
    private OcrParser() {
        super();
    }
    
    /**
     * OCRテキスト全体を解析してOcrItemリストを返す。
     *
     * @param rawText ML Kit から抽出されたテキスト
     * @return 解析結果のリスト。パターンにマッチしない行は空のOcrItemとして追加しない。
     *        ただし、全行がマッチしなかった場合は空のOcrItem1件を返し手動入力を促す。
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.rokusodo.healthcheckup.OcrItem> parse(@org.jetbrains.annotations.NotNull()
    java.lang.String rawText) {
        return null;
    }
    
    /**
     * 1行のテキストを解析してOcrItemに変換する。マッチしない場合はnullを返す。
     */
    private final com.rokusodo.healthcheckup.OcrItem parseLine(java.lang.String line) {
        return null;
    }
}