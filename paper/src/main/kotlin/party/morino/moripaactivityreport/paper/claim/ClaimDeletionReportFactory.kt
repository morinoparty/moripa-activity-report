/*
 * MoripaActivityReport
 * Copyright © 2026 morinoparty
 *
 * MoripaActivityReport is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * MoripaActivityReport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with MoripaActivityReport. If not, see <https://www.gnu.org/licenses/>.
 */
package party.morino.moripaactivityreport.paper.claim

import party.morino.moripaactivityreport.common.model.report.ActivityReport
import party.morino.moripaactivityreport.common.model.report.ReportActor
import party.morino.moripaactivityreport.common.model.report.ReportColor
import party.morino.moripaactivityreport.common.model.report.ReportField
import party.morino.moripaactivityreport.paper.model.claim.DeletedClaim

/**
 * 土地保護の削除の報告を組み立てる
 */
object ClaimDeletionReportFactory {
    /**
     * 報告を組み立てる (外部状態に依存しない純粋関数)
     *
     * @param executor 削除した人
     * @param claim 削除された土地保護
     * @param lastSeen 持ち主の最終ログインの表示 (例: 6日前)
     * @param reason 削除の理由 (/deleteclaim に入力された内容)
     * @return Discord に送る報告
     */
    fun create(executor: ReportActor, claim: DeletedClaim, lastSeen: String, reason: String): ActivityReport =
        ActivityReport(
            executor = executor,
            target = ReportActor(claim.ownerName, claim.ownerUuid),
            color = ReportColor.CLAIM_DELETION,
            fields = listOf(
                ReportField("処理", "土地保護削除"),
                ReportField("座標", "x: ${claim.x} z: ${claim.z}"),
                ReportField("最終ログイン", lastSeen),
                ReportField("理由", reason),
            ),
        )
}
