# タスク分解 — 画面遷移刷新（001）

> 依存順。各 Phase 完了時に本ファイルのチェックを更新する。詳細は `plan.md` 参照。

## Phase 0: ドキュメント整備

- [x] specs/001-screen-flow-renewal/（spec.md / plan.md / data-model.md / tasks.md）
- [x] DESIGN.md ドラフト（カテゴリ9色トークン）→ 代表承認待ち
- [x] docs/architecture.md に環境構成図（Mermaid）追加
- [ ] README に構成図セクション掲載
- [ ] リポジトリ CLAUDE.md 作成（DESIGN.md 準拠の明記）

## Phase 1: テスト基盤+リグレッション先行

- [ ] build.gradle にテスト依存追加（JUnit4 / Robolectric / navigation-testing / room-testing / coroutines-test）
- [ ] OcrParser 単体テスト（正常系・誤読・空入力）
- [ ] Room DAO テスト（in-memory）
- [ ] 既存 nav_graph 遷移テスト
- [ ] CI にテスト実行 workflow 追加
- [ ] `./gradlew test` 通過

## Phase 2: データモデル変更（★advisor 相談 / 不能時 G-5）

- [ ] MigrationTest（v1→v2 データ保持・シード投入）を先に作成
- [ ] ItemMaster に category / isFavorite / favoritedAt 追加
- [ ] Migration v1→2（ALTER TABLE + 差分シード投入）
- [ ] シード24項目＋カテゴリ定義（data-model.md どおり）
- [ ] FirestoreRepository 新フィールド対応（後方互換）
- [ ] web/src/types.ts 型整合

## Phase 3: 画面遷移再構成

- [ ] 新遷移図に合わせた遷移テストを先に作成
- [ ] HomeFragment（S-02: 3ボタン＋メニュー）
- [ ] RegisterMethodFragment（S-05）
- [ ] nav_graph 刷新（startDestination=home、認証ガード移設、既存4画面の導線維持）
- [ ] S-06a/S-06b 完了→Home への popUpTo

## Phase 4: S-03 / S-04

- [ ] ItemListFragment（カテゴリ色・♥お気に入り上部固定・登録順）
- [ ] TrendGraphFragment 改修（1年/全期間タブ・最新値表示。ViewModel テストファースト）

## Phase 5: S-06b / S-06a

- [ ] BmiCalculator（TDD）
- [ ] ManualEntryFragment（S-06b: マスタ順入力・BMI自動計算+上書き・⊕項目追加）
- [ ] OcrResultFragment に ⊕項目追加（補正フローは ViewModel テストファースト）

## Phase 6: S-07 / S-01

- [ ] ContactFragment（mailto Intent・メーラー不在エラー処理）
- [ ] S-01 仕様一致確認（Google SSO 維持）

## Phase 7: 仕上げ

- [ ] 全テスト通過・実機/エミュレータ通し確認（plan.md の検証シナリオ）
- [ ] README / docs/backlog.md / po_agent バックログ更新
- [ ] Notion / Obsidian タスク status 更新
- [ ] advisor 最終レビュー（不能時 G-5 明記）
