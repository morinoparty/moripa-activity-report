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
package party.morino.moripaactivityreport.velocity.punishment

import party.morino.moripaactivityreport.common.model.report.ActivityReport
import party.morino.moripaactivityreport.common.model.report.ReportActor
import party.morino.moripaactivityreport.common.model.report.ReportColor
import party.morino.moripaactivityreport.common.model.report.ReportField
import space.arim.libertybans.api.PunishmentType
import java.time.Duration
import java.time.Instant

/**
 * LibertyBans の処罰と、その解除 (恩赦) の報告を組み立てる
 */
object PunishmentReportFactory {
    /**
     * 処罰の報告を組み立てる (外部状態に依存しない純粋関数)
     *
     * @param executor 処罰した人 (コンソールの場合は null)
     * @param target 処罰された人
     * @param type 処罰の種類
     * @param start 処罰の開始日時
     * @param end 処罰の終了日時 (無期限の場合は null)
     * @param reason 処罰の理由
     * @return Discord に送る報告
     */
    fun createPunishment(
        executor: ReportActor?,
        target: ReportActor,
        type: PunishmentType,
        start: Instant,
        end: Instant?,
        reason: String,
    ): ActivityReport = ActivityReport(
        executor = executor,
        target = target,
        color = colorOf(type),
        fields = listOf(
            ReportField("処罰", type.name),
            ReportField("期間", durationOf(start, end).toString()),
            ReportField("理由", reason),
        ),
    )

    /**
     * 処罰の解除 (恩赦) の報告を組み立てる (外部状態に依存しない純粋関数)
     *
     * @param executor 解除した人 (コンソールの場合は null)
     * @param target 処罰を解除された人
     * @param type 解除された処罰の種類
     * @return Discord に送る報告
     */
    fun createPardon(executor: ReportActor?, target: ReportActor, type: PunishmentType): ActivityReport = ActivityReport(
        executor = executor,
        target = target,
        color = ReportColor.PARDON,
        fields = listOf(ReportField("恩赦", type.name)),
    )

    /**
     * 処罰の期間を求める (外部状態に依存しない純粋関数)
     *
     * 無期限の処罰とキックは期間を持たないため 0 (PT0S と表示される) にする。
     *
     * @param start 処罰の開始日時
     * @param end 処罰の終了日時 (無期限の場合は null)
     * @return 処罰の期間
     */
    fun durationOf(start: Instant, end: Instant?): Duration =
        if (end == null) Duration.ZERO else Duration.between(start, end).coerceAtLeast(Duration.ZERO)

    /**
     * 処罰の種類ごとの Embed の色を返す
     *
     * @param type 処罰の種類
     * @return Embed の色
     */
    private fun colorOf(type: PunishmentType): Int = when (type) {
        PunishmentType.BAN -> ReportColor.BAN
        PunishmentType.MUTE -> ReportColor.MUTE
        PunishmentType.WARN -> ReportColor.WARN
        PunishmentType.KICK -> ReportColor.KICK
    }
}
