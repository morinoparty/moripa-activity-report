# MoripaActivityReport

morinoparty のサーバー運営での操作 (アクティビティ) を Discord Webhook に報告する Minecraft プラグインです。Paper / Velocity の両方に対応しています。

- **土地保護の削除 (Paper)**: GriefPrevention の `/deleteclaim [理由]` で削除した土地保護を、理由と一緒に報告します。理由に `close #番号` を含めると、[MoripaUtils](https://github.com/morinoparty/moripa-utils) のチケットをクローズします。
- **処罰 (Velocity)**: LibertyBans の BAN・ミュート・警告・キックと、その解除を理由と一緒に報告します。

コマンドは Paper が `/mar`、Velocity が `/marv` です。設定や使い方は `docs/content/` のドキュメントを参照してください。

## ビルド

```bash
task build   # ./gradlew clean build
```

JAR は `paper/build/libs/` と `velocity/build/libs/` に出力されます。

## ライセンス

LibertyBans の API を利用しているため、[GNU Affero General Public License v3.0 以降](LICENSE) で公開しています。
