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

import party.morino.moripaactivityreport.common.model.ticket.TicketCloseDirective
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * 処罰の確定前に理由から取り出したチケットのクローズの指示を、確定後まで預かる
 *
 * 理由から「close #番号」を取り除いた後の処罰 (種類・対象・実行者・理由) を鍵にして、確定後のイベントで取り出す。
 * 他のプラグインに取り消されたなどで確定しなかった処罰の指示は、一定時間後に捨てる。
 *
 * @param expiration 指示を預かる時間
 */
class PendingTicketCloses(
    private val expiration: Duration = DEFAULT_EXPIRATION,
) {
    /** 鍵 → (指示, 預けた日時) */
    private val entries = ConcurrentHashMap<String, Pair<TicketCloseDirective, Instant>>()

    /**
     * 指示を預ける
     *
     * @param key 処罰を表す鍵
     * @param directive クローズの指示
     * @param now 現在日時
     */
    fun put(key: String, directive: TicketCloseDirective, now: Instant = Instant.now()) {
        prune(now)
        entries[key] = directive to now
    }

    /**
     * 指示を取り出す (取り出した指示は消える)
     *
     * @param key 処罰を表す鍵
     * @param now 現在日時
     * @return 預けた指示 (無い場合や期限切れの場合は null)
     */
    fun take(key: String, now: Instant = Instant.now()): TicketCloseDirective? {
        prune(now)
        return entries.remove(key)?.first
    }

    /**
     * 期限切れの指示を捨てる
     *
     * @param now 現在日時
     */
    private fun prune(now: Instant) {
        entries.entries.removeIf { (_, value) -> Duration.between(value.second, now) > expiration }
    }

    companion object {
        /** 指示を預ける時間の既定値 (処罰の確定はふつう数秒以内に終わる) */
        val DEFAULT_EXPIRATION: Duration = Duration.ofMinutes(5)

        /**
         * 処罰を表す鍵を作る (外部状態に依存しない純粋関数)
         *
         * @param type 処罰の種類
         * @param victim 処罰の対象
         * @param operator 処罰した人
         * @param reason 処罰の理由 (指示を取り除いた後のもの)
         * @return 鍵
         */
        fun keyOf(type: Any, victim: Any, operator: Any, reason: String): String = "$type|$victim|$operator|$reason"
    }
}
