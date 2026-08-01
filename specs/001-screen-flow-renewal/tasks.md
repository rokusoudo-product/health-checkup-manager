# タスク分解 — 画面遷移刷新（001）

> 依存順。各 Phase 完了時に本ファイルのチェックを更新する。詳細は `plan.md` 参照。

## Phase 0: ドキュメント整備 ✅

- [x] specs/001-screen-flow-renewal/（spec.md / plan.md / data-model.md / tasks.md）
- [x] DESIGN.md ドラフト（カテゴリ9色トークン）→ **代表承認待ち**
- [x] docs/architecture.md に環境構成図（Mermaid）追加
- [x] README に構成図セクション掲載
- [x] リポジトリ CLAUDE.md 作成（DESIGN.md 準拠の明記）

## Phase 1: テスト基盤+リグレッション先行 ✅

- [x] build.gradle にテスト依存追加（JUnit4 / Robolectric / navigation-testing / room-testing / coroutines-test）
- [x] OcrParser 単体テスト（正常系・誤読・空入力）
- [x] Room DAO テスト（in-memory）
- [x] 既存 nav_graph 遷移テスト
- [x] CI にテスト実行 workflow 追加（.github/workflows/android-test.yml）
- [x] `./gradlew test` 通過

## Phase 2: データモデル変更 ✅（advisor 利用不能のため G-5 適用: MigrationTest+差分レビューで代替）

- [x] MigrationTest（v1→v2 データ保持・シード投入）を先に作成
- [x] ItemMaster に category / isFavorite / favoritedAt 追加
- [x] Migration v1→2（ALTER TABLE + 差分シード投入）
- [x] シード25項目＋カテゴリ定義（data-model.md どおり）
- [x] FirestoreRepository 新フィールド対応（後方互換）
- [x] web/src/types.ts 型整合（tsc -b 通過）

## Phase 3: 画面遷移再構成 ✅

- [x] 新遷移図に合わせた遷移テストを先に作成
- [x] HomeFragment（S-02: 3ボタン＋メニュー）
- [x] RegisterMethodFragment（S-05）
- [x] nav_graph 刷新（startDestination=home、既存4画面の導線維持）
- [x] S-06a/S-06b 完了→Home への popBackStack

## Phase 4: S-03 / S-04 ✅

- [x] ItemListFragment（カテゴリ色・♥お気に入り上部固定・登録順）
- [x] TrendGraphFragment 改修（1年/全期間タブ・最新値表示。ロジックはテストファースト）

## Phase 5: S-06b / S-06a ✅

- [x] BmiCalculator（TDD）
- [x] ManualEntryFragment（S-06b: マスタ順入力・BMI自動計算+上書き・⊕項目追加）
- [x] OcrResultFragment に ⊕項目追加（補正フローは OcrItemAdapterTest で検証）

## Phase 6: S-07 / S-01 ✅

- [x] ContactFragment（mailto Intent・メーラー不在エラー処理）
    - [x] contact_email の実アドレス確定（2026-08-01 代表決定: `info@rokusoudo.co.jp`）
- [x] S-01 仕様一致確認（Google SSO 維持。MainActivity の認証スキップTODOは現行踏襲）

## Phase 7: 仕上げ

- [x] 全テスト通過（38件）+ assembleDebug 成功
- [ ] 実機/エミュレータでの通し確認（既存タスク「健診ノート: 実機/エミュレータ動作確認」と連携して実施）
- [x] README / docs/backlog.md 更新
- [x] Notion / Obsidian タスク status 更新
- [x] advisor 最終レビュー → 利用不能のため G-5 適用（スキップを明記）
