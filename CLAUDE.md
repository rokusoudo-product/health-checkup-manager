# CLAUDE.md — health-checkup-manager

健康診断結果の記録・可視化アプリ（Android 主体 + Web 閲覧/手入力）。

## 必読ドキュメント

- `specs/001-screen-flow-renewal/` — 進行中の画面遷移刷新の仕様・計画・タスク（正本は Obsidian `Dropbox/obsidian/task/specs/20260718-health-app-screen-flow.md`）
- `docs/architecture.md` — アーキテクチャ・環境構成図
- `DESIGN.md` — UIデザイン仕様。**UIは DESIGN.md に準拠する**（カラーコード・サイズの直書き禁止、トークン参照のみ）

## 開発ルール

- 仕様変更はコードより先に spec（正本→リポジトリコピー）を更新してから実装する
- TDD: 画面遷移ロジック・BMI計算・OCR補正フローはテストファースト。既存機能に触れる前にリグレッションテストを用意
- 医療的助言・診断の文言は一切表示しない（薬事法対応。`docs/compliance.md`）
- Android は View/XML + Material Components を継続（Compose 移行は別タスクで判断）

## ビルド・テスト

```bash
cd android && ./gradlew test          # 単体テスト
cd android && ./gradlew assembleDebug # APK ビルド
cd web && npm run build               # Web ビルド
```
