import type { BaseLayoutProps } from "fumadocs-ui/layouts/shared";

import { appName, docsRoute, gitConfig } from "./shared";

export function baseOptions(): BaseLayoutProps {
    return {
        nav: {
            title: <span className="text-lg font-bold">{appName}</span>,
            url: docsRoute,
            // モバイルのヘッダーは、ページの先頭では背景を透過し、スクロールしたら背景を付ける
            transparentMode: "top",
        },
        // ダークモードは提供しないため、テーマ切り替えを表示しない
        themeSwitch: {
            enabled: false,
        },
        githubUrl: `https://github.com/${gitConfig.user}/${gitConfig.repo}`,
    };
}
