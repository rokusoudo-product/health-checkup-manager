# データモデル変更 — 画面遷移刷新（001）

## 変更概要

| 対象 | 変更 |
|------|------|
| Room `ItemMaster` | `category: String` / `isFavorite: Boolean = false` / `favoritedAt: Long?` を追加 |
| Room スキーマ | version 1 → 2（Migration で ALTER TABLE + マスタ差分投入） |
| Firestore `users/{uid}/itemMasters` | 同フィールドを追加（欠損時デフォルトで後方互換） |
| Web `web/src/types.ts` | `ItemMaster` に `category?` / `isFavorite?` を追加（UI 変更なし） |

`ExaminationRecord` / `ExaminationItem` は変更なし。

## Migration v1→v2 の方針

初期シードは Room の `onCreate` コールバックでしか走らないため、既存端末には Migration で反映する。

1. `ALTER TABLE item_masters ADD COLUMN category TEXT NOT NULL DEFAULT 'その他'`
2. `ALTER TABLE item_masters ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0`
3. `ALTER TABLE item_masters ADD COLUMN favoritedAt INTEGER`
4. 既存シード10項目へ `category` を UPDATE
5. 新規マスタ項目を `INSERT OR IGNORE`（ユーザーが同名項目を作成済みでも上書きしない）

## マスタシード定義（v2）

基準値は一般的な健診・特定健診の参考値。**表示・ハイライトのみに使用し医療診断には使わない**（薬事法対応、現行方針踏襲）。性別依存（♀♂で範囲が異なる）項目は null とし、項目マスター管理画面から編集可能。

| カテゴリ | 項目名 | 単位 | 下限 | 上限 | 備考 |
|----------|--------|------|------|------|------|
| 身体計測 | 身長 | cm | — | — | |
| 身体計測 | 体重 | kg | — | — | |
| 身体計測 | BMI | kg/m2 | 18.5 | 25.0 | S-06b で自動計算+上書き可 |
| 身体計測 | 腹囲 | cm | — | — | 性別依存（男85/女90） |
| 血圧 | 収縮期血圧 | mmHg | — | 129 | 既存 |
| 血圧 | 拡張期血圧 | mmHg | — | 84 | 既存 |
| 血液一般 | 血色素量(Hb) | g/dL | — | — | 性別依存 |
| 血液一般 | 赤血球数 | 万/μL | — | — | 性別依存 |
| 血液一般 | 白血球数 | /μL | 3100 | 8400 | |
| 血液一般 | 血小板数 | 万/μL | 14.5 | 32.9 | |
| 脂質 | LDLコレステロール | mg/dL | — | 139 | 既存 |
| 脂質 | HDLコレステロール | mg/dL | 40 | — | 既存 |
| 脂質 | 中性脂肪 | mg/dL | — | 149 | 既存 |
| 肝機能 | AST(GOT) | U/L | — | 30 | 既存 |
| 肝機能 | ALT(GPT) | U/L | — | 30 | 既存 |
| 肝機能 | γ-GTP | U/L | — | 50 | 既存 |
| 腎機能 | クレアチニン | mg/dL | — | — | 性別依存 |
| 腎機能 | eGFR | mL/min/1.73m2 | 60 | — | |
| 腎機能 | 尿酸 | mg/dL | — | 7.0 | |
| 糖代謝 | 空腹時血糖 | mg/dL | — | 99 | 既存 |
| 糖代謝 | HbA1c | % | — | 5.5 | |
| 尿検査 | 尿蛋白 | 定性 | — | — | 定性値（−/±/+）。グラフ対象外 |
| 尿検査 | 尿糖 | 定性 | — | — | 定性値。グラフ対象外 |
| その他 | 視力(右) | — | — | — | グラフ対象外 |
| その他 | 視力(左) | — | — | — | グラフ対象外 |

## お気に入りの並び順（Q2: 登録順）

- ♥ON: `favoritedAt = System.currentTimeMillis()` / ♥OFF: `favoritedAt = null, isFavorite = false`
- S-03 の並び: お気に入り（`favoritedAt` 昇順）→ 非お気に入り（カテゴリ順→項目名順）

## グラフ対象の判定（S-04）

`ExaminationItem.value` が数値にパースできる項目のみグラフ対象。定性値（尿蛋白等）・視力は S-03 に表示するがタップ時は「グラフ対象外」の扱い（値一覧表示 or 無効化。実装時に確定）。
