# MoripaActivityReport

morinoparty のサーバー運営での操作 (アクティビティ) を Discord Webhook に報告する Minecraft プラグインです。Paper / Velocity の両方に対応しています。

- **土地保護の削除 (Paper)**: GriefPrevention の `/deleteclaim [理由]` で削除した土地保護を、理由と一緒に報告します。理由に `close #番号` を含めると、[MoripaUtils](https://github.com/morinoparty/moripa-utils) のチケットをクローズします。
- **処罰 (Velocity)**: LibertyBans の BAN・ミュート・警告・キックと、その解除を理由と一緒に報告します。

コマンドは Paper が `/mar`、Velocity が `/marv` です。設定や使い方は `docs/content/` のドキュメントを参照してください。

## ビルド

MoripaUtils の公開 API を GitHub Packages から取得するため、`read:packages` 権限を持つ GitHub のトークンが必要です。
`~/.gradle/gradle.properties` に設定するか、環境変数 `GITHUB_ACTOR` / `GITHUB_TOKEN` で渡してください。

```properties
gpr.user=<GitHub のユーザー名>
gpr.key=<read:packages 権限を持つトークン (gh auth token でも可)>
```

```bash
task build   # ./gradlew clean build
```

JAR は `paper/build/libs/` と `velocity/build/libs/` に出力されます。

## ライセンス

LibertyBans の API を利用しているため、[GNU Affero General Public License v3.0 以降](LICENSE) で公開しています。
