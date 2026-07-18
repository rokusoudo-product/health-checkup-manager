# 健診ツール 画面遷移・画面仕様刷新（S-01〜S-07）実装計画

## Context

仕様書 `Dropbox/obsidian/task/specs/20260718-health-app-screen-flow.md`（v0.2、未決事項 Q1〜Q9 は 2026-07-18 代表回答で確定済み）に基づき、health-checkup-manager の画面遷移・画面仕様を刷新する。対象は **Android のみ**（Q5）。Web は型定義の整合のみ取り、現状維持。ゴールは S-01〜S-07 が遷移図どおりに動作し、既存機能（OCR登録・グラフ・Firestore同期）が壊れていないこと。OCR精度チューニング・デザイン作り込みはスコープ外（Notion起票済み）。

- リポジトリ: WSL `~/health-checkup-manager`（github.com/rokusoudo-product/health-checkup-manager, main）
- 現状: Sprint 6 完了。Android は Fragment + Navigation + MVVM + Room(v1) + Firestore同期。**テストコード0件**
- 進め方: feature ブランチ `feature/screen-flow-renewal` + PR 単位。段階的リリースは不要（Google Play 未公開・一般ユーザー不在）

## Phase 0: ドキュメント整備（spec-kit フロー）

1. repo に `specs/001-screen-flow-renewal/` を作成: spec.md（仕様書v0.2を整形コピー）/ plan.md（本計画）/ data-model.md / tasks.md
2. `DESIGN.md` ドラフト作成（DESIGN_STANDARDS.md 準拠）。**カテゴリ9分類の色トークン**（身体計測/血圧/血液一般/脂質/肝機能/腎機能/糖代謝/尿検査/その他）を定義 → **代表承認（デザイン基準ファーストの承認ポイント）**
3. `docs/architecture.md`（Mermaid: Android / Room / Firebase Auth / Firestore / Web）更新 + README に構成図セクション掲載
4. リポジトリ CLAUDE.md（あれば）に「UIは DESIGN.md に準拠」を追記

## Phase 1: テスト基盤 + リグレッションテスト先行（現状0件のため最優先）

- `android/app/build.gradle` にテスト依存を追加: JUnit4 / Robolectric / androidx.navigation-testing / room-testing / kotlinx-coroutines-test
- 既存機能のリグレッションテストを**実装変更前に**作成:
  - `OcrParser.parse()` 単体テスト（正常系・誤読・空入力。DoD で必須とされながら未実施）
  - Room DAO テスト（in-memory: 記録保存・ItemTrend 取得・基準値外件数）
  - 既存 nav_graph 遷移テスト（login→main→camera→ocrResult、main→detail→graph 等）
- `.github/` の既存 workflow にテスト実行ステップを追加（存在すれば）

## Phase 2: データモデル変更（★advisor 相談ポイント。利用不能時は G-5 適用しdiffレビュー+テストで代替）

- `ItemMaster` に追加: `category: String` / `isFavorite: Boolean = false` / `favoritedAt: Long?`（お気に入り登録順=Q2用）
- Room v1→2 Migration: ALTER TABLE ×3 + **マスタ差分投入**（初期シードは onCreate 時のみのため、既存10項目への category UPDATE と新項目 INSERT OR IGNORE を migration で実施）
- シード拡充（Q1c）: 仕様書「検査項目マスタ一覧」の約24項目をカテゴリ付きで定義。性別依存項目（Hb・腹囲・クレアチニン等）は基準値 null（項目マスター画面で編集可能）
- `FirestoreRepository.saveItemMaster/fetchItemMasters` に新フィールド追加（欠損時デフォルトで後方互換）
- `web/src/types.ts` の `ItemMaster` に `category?` / `isFavorite?` を追加（型整合のみ、Web UI 変更なし）
- テストファースト: MigrationTest（room-testing）で v1→v2 のデータ保持・シード投入を先に書く

## Phase 3: 画面遷移再構成（nav_graph 刷新）

- 新 `HomeFragment`（S-02）: 3ボタン（登録→S-05 / グラフ📈→S-03 / お問い合わせ→S-07）。既存 `menu_main.xml`（項目マスター・基準値外一覧・ログアウト）を流用し、**記録一覧**をメニューに追加（Q7: 既存4画面存続）
- `nav_graph.xml`: startDestination を homeFragment に変更。認証ガード（未ログイン→loginFragment）は現行 MainFragment の方式を Home に移設
- 新 `RegisterMethodFragment`（S-05）: 📷カメラ→cameraFragment / ✎手入力→manualEntryFragment
- S-06a/S-06b の登録完了→ popUpTo で Home へ戻す
- 既存 `MainFragment`（記録一覧）→ 記録詳細 → グラフ の動線は存続
- 遷移ロジックはテストファースト（Phase 1 の遷移テストを新遷移図に合わせ拡張してから実装）

## Phase 4: S-03 項目一覧 + S-04 グラフ改修

- 新 `ItemListFragment`（S-03）: 全マスタ項目をカテゴリ色分け表示（DESIGN.md トークン→ colors.xml）。♥トグル（favoritedAt=now/null、ONは赤♥）、お気に入りは上部固定・登録順（Q2）。行タップ→ TrendGraphFragment(itemName)
- `TrendGraphFragment`（S-04）: 期間切替タブ **1年/全期間**（Q3）+ 最新値表示を追加。期間フィルタは ViewModel でテストファースト。グラフ対象は数値項目のみ（定性値・視力は除外）

## Phase 5: S-06b 手入力 + S-06a 項目追加

- 新 `ManualEntryFragment`（S-06b）: マスタ順の入力欄+単位（カテゴリ色分け）、**BMI自動計算+手動上書き可**（Q8。`BmiCalculator` ユーティリティを TDD で新規作成）、⊕項目追加、保存は既存 `HealthRepository`（Room+Firestore）を流用。Web `RecordForm.tsx` の動的項目行ロジックを移植参考にする
- `OcrResultFragment`（S-06a）: **⊕項目追加**行を追加（`OcrItemAdapter` 拡張）。OCR候補の確認・補正フローは `OcrResultViewModel` にロジックを寄せてテストファースト

## Phase 6: S-07 お問い合わせ + S-01 確認

- 新 `ContactFragment`（S-07）: 連絡先表示 + お名前/お問い合わせ内容入力 → **mailto Intent**（ACTION_SENDTO。宛先 info@〜 は設定値として保持 ※実アドレスは実装時に代表確認）。メーラー不在時のエラー表示
- S-01: 変更なし（Google SSO 維持=Q6）。文言・遷移の仕様一致確認のみ

## Phase 7: 仕上げ・検証

- 全テスト通過（`./gradlew test`）+ 実機/エミュレータで S-01〜S-07 を遷移図どおり通し確認（既存 Notion タスク「実機/エミュレータ動作確認」と連携）
- README / architecture.md / repo `docs/backlog.md` / po_agent 側バックログを更新
- Notion 本体タスクを完了へ、Obsidian タスクノート status 更新
- 完了宣言前に advisor 最終レビュー（利用不能時は G-5: 明記の上スキップ）

## 検証方法

1. `cd ~/health-checkup-manager/android && ./gradlew test`（単体・Robolectric・Migration）
2. エミュレータ or Pixel 7a で通しシナリオ: ログイン→ホーム→登録→カメラOCR→補正+項目追加→保存→ホーム→登録→手入力(BMI自動計算確認)→保存→グラフ→項目一覧(カテゴリ色・♥固定)→項目グラフ(1年/全期間タブ)→お問い合わせ(メーラー起動)→メニューから記録一覧・項目マスター・基準値外一覧・ログアウト
3. 既存データ端末での Migration 確認（v1 DB を持つ状態でアップデート→データ保持・新マスタ投入）

## リスク・調整事項

- **applicationId 不一致修正（rokusodo→rokusoudo）タスクが未着手（期限7/21）**: パッケージ変更は全ファイルに波及するため、**本改修の前に先行実施を推奨**（コンフリクト回避）
- Firebase Console 設定タスク（Firestore有効化等）未着手: 同期の実機検証に必要。開発自体はローカル Room で進行可能
- advisor ツールは現セッションで利用不能。Phase 2 前の相談は実装セッションで再試行し、不能時は G-5 適用を成果物報告に明記
