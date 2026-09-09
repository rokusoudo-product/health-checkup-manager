# DESIGN.md — health-checkup-manager

> 上位規範: `C:\Users\moets\.claude\DESIGN_STANDARDS.md`（全プロジェクト共通デザイン基準）
> 作成: 2026-07-18（画面遷移刷新 001 の plan 工程で作成）

## 基本方針

- 対象プラットフォーム: Android（min API 26）。Web 版は本書のトークンを参照するが今回の刷新対象外（2026-08-12 追記: Web の経年グラフ画面【Issue #28】以降に追加する Web 新規画面には、下記「Web 版トークン適用方針」を適用する。2026-09-08 追記: 既存 Web 4画面のうち `ItemMasters` と経年グラフ画面は既にトークン化済みで直書きは0件。残る `RecordList` / `RecordDetail` / `RecordForm` のカラーコード直書きの是正は、トークン基盤を整備する Issue #56 を前提として、画面別の後続 Issue #57（共通シャーシ）・#58（ログイン画面）・#59（記録一覧）・#60（記録詳細・入力フォーム）が担当する）
- 準拠ガイドライン: Material Design 3
- 使用コンポーネントライブラリ: Material Components for Android（XML/View ベース）
- トーン&マナー: 落ち着いた健康管理ツール。医療データを扱うため誇張のない誠実な配色

## カラートークン

| トークン | ライト | ダーク | 用途 |
|---------|-------|-------|------|
| primary | #00696C | #4DD8DC | 主要アクション（現行テーマ踏襲のティール系） |
| background | #FAFDFC | #191C1C | 画面背景 |
| surface | #FFFFFF | #252828 | カード・パネル |
| text-primary | #191C1C | #E0E3E2 | 本文 |
| text-secondary | #3F4948 | #BEC8C7 | 補助テキスト |
| error | #BA1A1A | #FFB4AB | エラー・基準値外ハイライト |
| warning | #B8860B | #FFD54F | 注意 |
| success | #2E7D32 | #81C784 | 成功 |
| favorite | #D32F2F | #EF5350 | お気に入り♥（ON時） |
| outline | #e5e4e7 | 未定義 | 枠線（2026-09-08 追記, Issue #56。Web の `--border` 用に新設。現行 Web の値 `#e5e4e7` をそのまま採用し、背景 `#FAFDFC` に対して枠線として視認できることを確認した。Android では未使用のためダーク値は未定義） |
| on-primary | #FFFFFF | 未定義 | primary 背景上の文字色（2026-09-09 追記, Issue #57。Web専用。Android colors.xml の既存 `on_primary`（#FFFFFFFF）と同値のため、Android 側の新規追記は不要と判断。ダークは Web 対象外のため未定義） |
| primary-hover | #00595C | 未定義 | primary 系コントロールの hover/pressed 背景（2026-09-09 追記, Issue #57。Web専用。確定 primary `#00696C` を基準に、`color-mix(in srgb, var(--color-primary) 85%, black)` 相当（primary 85% + 黒 15%）で算出。旧 Web 実装の `#155f55` は旧 primary `#1a7a6e` から導出された値のため系統が異なり、そのまま採用していない。ダークは Web 対象外のため未定義） |
| disabled | #aaaaaa | 未定義 | 無効状態の背景・主張を抑えたアイコン文字色（2026-09-09 追記, Issue #57。Web専用。現行 Web の値 `#aaa` をそのまま採用。WCAG 1.4.3 は無効化されたUIコンポーネントを対象外としているためコントラスト要件の対象外。ダークは Web 対象外のため未定義） |
| hover-neutral | #f5f5f5 | 未定義 | ニュートラル系ボタン（`.btn-secondary` 等）の hover 背景（2026-09-09 追記, Issue #57。Web専用。現行 Web の値 `#f5f5f5` をそのまま採用。ダークは Web 対象外のため未定義） |

### カテゴリカラー（検査項目の文字・枠色。2026-07-18 決定 Q1）

| カテゴリ | ライト | ダーク |
|----------|-------|-------|
| 身体計測 | #00796B | #4DB6AC |
| 血圧 | #C62828 | #EF9A9A |
| 血液一般 | #AD1457 | #F48FB1 |
| 脂質 | #B34700 | #FFB74D |
| 肝機能 | #2E7D32 | #81C784 |
| 腎機能 | #1565C0 | #64B5F6 |
| 糖代謝 | #6A1B9A | #CE93D8 |
| 尿検査 | #946200 | #FFF176 |
| その他 | #546E7A | #B0BEC5 |

- 実装は `res/values/colors.xml` / `res/values-night/colors.xml` にトークン名で定義し、レイアウト・コードからは **トークン参照のみ**（直書き禁止）
- カテゴリ色は項目行の**枠線＋項目名テキスト**に適用。背景には使わない（コントラスト確保）
- 色だけに依存しない: カテゴリ名ラベル（テキスト）を必ず併記
- **コントラスト要件**: 文字色として使うため WCAG 2.1 AA（4.5:1）を満たすこと。ライトは背景 `#FAFDFC`、ダークは `#191C1C` に対して測定する。カテゴリ色を追加・変更したら必ず再測定する
- 2026-08-01 改訂（代表承認）: ライトの脂質 `#EF6C00`（3.01:1）と尿検査 `#F9A825`（1.93:1）が AA 未達だったため差し替え。脂質は血圧 `#C62828` との混同を避けるためオレンジ味を残す `#B34700`（5.37:1）、尿検査は `#946200`（5.12:1）を採用。ダーク9色は全て 7.03:1 以上のため変更なし。現行値はライト 5.01〜9.18:1

## タイポグラフィ

- フォント: システムデフォルト（日本語）
- スケール: 12 / 14 / 16 / 20 / 24 / 32 sp（共通基準どおり）

## 余白・レイアウト

- 8pt グリッド準拠
- 画面の基本構造: 上部 AppBar + コンテンツ（下部ナビなし。ホームハブ型遷移）
- タップ対象は最低 48×48dp

## 画面一覧と状態

| 画面 | 目的 | 主要アクション | 4状態の特記事項 |
|------|------|--------------|----------------|
| S-01 ログイン | Google SSO 認証 | Googleでログイン | エラー: 認証失敗トースト |
| S-02 ホーム | 3機能への分岐 | 登録 / グラフ / お問い合わせ | 空状態なし（静的ハブ） |
| S-03 項目一覧 | 項目選択・お気に入り | 項目タップ / ♥トグル | 空: マスタ0件時の案内 |
| S-04 グラフ表示 | 推移確認 | 期間タブ切替 | 空: 記録0件時「データがありません」 |
| S-05 登録方法選択 | 入力方式の二択 | カメラ / 手入力 | 静的画面 |
| S-06a カメラ読み取り | 撮影→OCR確認・補正 | 撮影 / この内容で登録 | ローディング: OCR処理中表示必須 |
| S-06b 手入力フォーム | 数値入力 | 登録する | エラー: 数値バリデーション |
| S-07 お問い合わせ | サポート連絡 | 送信（メーラー起動） | エラー: メーラー不在時の案内 |

## Web 版トークン適用方針（2026-08-12 追記, Issue #28。2026-09-08 改訂, Issue #56）

Web（`web/`）で新規に追加する画面は、Android と同じ意味のトークンを CSS カスタムプロパティとして参照する。カラーコードの直書きは禁止。

- **Web はライト固定であり、ダークモードは対象外**（2026-09-08 代表判断）。`web/src/index.css` の `:root` に `color-scheme: light` を明示し、`@media (prefers-color-scheme: dark)` によるトークンのダーク値定義は行わない。
  Android は `res/values/colors.xml` / `res/values-night/colors.xml` の2本立てでダークモードに対応しているため、**Android はダーク対応・Web はライト固定**という差異が生じる。これは意図した判断であり、Web のダーク対応漏れではない。
- 定義場所: `web/src/index.css` の `:root`（ライト値のみ）
- 命名: `--color-<トークン名>`（Android のトークン表と同じ名前を使う）
- 現時点で定義済みのトークン（ライト値のみ。Issue #56 で `primary` / `error` 以外を追加。Issue #57 で `on-primary` / `primary-hover` / `disabled` / `hover-neutral` を追加）:

| CSS変数 | トークン | ライト | 用途 |
|--------|---------|-------|------|
| `--color-primary` | primary | #00696C | グラフの主線・アクティブなタブなど主要アクション相当 |
| `--color-background` | background | #FAFDFC | 画面背景 |
| `--color-surface` | surface | #FFFFFF | カード・パネル |
| `--color-text-primary` | text-primary | #191C1C | 本文 |
| `--color-text-secondary` | text-secondary | #3F4948 | 補助テキスト |
| `--color-error` | error | #BA1A1A | 基準値の上限・下限を示す参照線 |
| `--color-warning` | warning | #B8860B | 注意 |
| `--color-success` | success | #2E7D32 | 成功 |
| `--color-favorite` | favorite | #D32F2F | お気に入り♥（ON時） |
| `--color-outline` | outline | #e5e4e7 | 枠線 |
| `--color-on-primary` | on-primary | #FFFFFF | navbar・`.btn-primary`・`.btn-logout` など primary 背景上の文字色（2026-09-09 追記, Issue #57） |
| `--color-primary-hover` | primary-hover | #00595C | `.btn-primary:hover` の背景（2026-09-09 追記, Issue #57） |
| `--color-disabled` | disabled | #aaaaaa | `.btn-primary:disabled` の背景 / `.btn-remove` の既定文字色（2026-09-09 追記, Issue #57） |
| `--color-hover-neutral` | hover-neutral | #f5f5f5 | `.btn-secondary:hover` の背景（2026-09-09 追記, Issue #57） |

- 通常の CSS（`color` / `background` 等）では `var(--color-primary)` のようにそのまま参照する
- **Canvas 描画（Chart.js 等）では `var()` がブラウザの Canvas 2D API 上で解決されないため**、`getComputedStyle(document.documentElement).getPropertyValue('--color-primary')` で実測値の文字列を取得してから渡す（`web/src/components/TrendChart.tsx` 参照）
- 新規トークンが必要になったら、この表と Android 側のカラートークン表の両方に追記し、値を一致させる
- 既存 Web 4画面のうち `ItemMasters` と経年グラフ画面は既にトークン化済みで直書きは0件。残る `RecordList` / `RecordDetail` / `RecordForm` の直書きカラーコードの是正は、本トークン基盤（Issue #56）を前提として画面別の後続 Issue #57〜#60 が担当する
- 移行用エイリアス（`--text-h` / `--text` / `--bg` / `--border`）: `App.css` の既存参照が壊れないよう、新トークンの `var()` 別名として `index.css` に一時的に残置していたが、Web カラートークン移行（Issue #56〜#60, #72）の完了に伴い、参照箇所（`App.css` / `index.css` 計25箇所）をすべて新トークン名へ置き換えたうえで Issue #72 にて撤去済み（`index.css` からエイリアス4定義を削除）。以後、旧トークン名（`--text-h` / `--text` / `--bg` / `--border`）は使用しない
- **外部ブランド由来の色は例外として直書きを許容する**（2026-09-09 追記, Issue #58）: `.btn-google`（Google サインインボタン）の配色 `#fff` / `#3c4043` / `#dadce0` は Google ブランドガイドラインの指定色であり、プロダクトのブランドカラーではないため、トークンへ機械的に寄せない。直書きのまま残し、理由をコード側のコメントで明示する。`--color-surface` の `#FFFFFF` とはたまたま同じ値になるだけで意味が異なるため代用しない

## プロジェクト固有ルール

- **View/XML ベースを継続**: 共通基準は Material3 Compose を第一候補とするが、既存実装が Fragment+XML（Material Components）で安定しているため、本刷新では View ベースを維持する（Compose 全面移行はスコープ外・別タスクで判断）
- 基準値外の値は error トークンでハイライト（現行踏襲）。医療的助言の文言は一切表示しない（薬事法対応）
- お気に入り♥は favorite トークン固定（カテゴリ色と混同させない）
