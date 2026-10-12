## このアプリケーションの概要
「MoripaActivityReport」という、morinoparty 向けの Minecraft プラグインです。
リポジトリ名は `moripa-activity-report` です。

## 目的
サーバー運営上の操作 (アクティビティ) を Discord Webhook に報告します。

- **claim** (Paper) - GriefPrevention の `/deleteclaim <message>` で土地を削除したときに、理由 (message) と一緒に報告する。message に `close #<番号>` が含まれる場合は、MoripaUtils のチケットをクローズする
- **punishment** (Velocity) - LibertyBans の処罰 (BAN / ミュート / 警告 / キック) と解除を、理由と一緒に報告する

コマンドは Paper が `/mar`、Velocity が `/marv` です。

## ライセンス
LibertyBans の API (AGPL-3.0-or-later) を利用するため、このプロジェクトも GNU AGPL v3.0 以降で公開します。

## 主な技術スタック
- Kotlin (言語)
- Koin (依存性注入)
- PaperMC (MinecraftプラグインAPI)
- Velocity (MinecraftプロキシAPI)
- Cloud (コマンドフレームワーク)
- MCCoroutine (非同期処理)
- GriefPrevention (土地保護。claim 機能の連携先)
- LibertyBans (処罰管理。punishment 機能の連携先)
- MoripaUtils (チケットのクローズに公開 API を使う)

## モジュール構成
- `common` - Paper/Velocity共通コード
- `paper` - Paper (Bukkit) プラグイン
- `velocity` - Velocity プロキシプラグイン
- `api` - 外部プラグイン向け公開API
