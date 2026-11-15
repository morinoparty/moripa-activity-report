export const appName = "MoripaActivityReport";
// ドキュメントはサイトのルートに置く (トップページがドキュメントの index になる)
export const docsRoute = "/";

export const gitConfig = {
    user: "morinoparty",
    repo: "moripa-activity-report",
    branch: "main",
};

// BASE_URL は Vite の base (例: /MoripaActivityReport/<sha>/docs/、GitHub Pages のカスタムドメインなら /)。静的ファイルの URL には base を付ける
export const searchIndexUrl = `${import.meta.env.BASE_URL}api/search.json`;
