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

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripaactivityreport.paper.claim.ClaimDeletionListener.Companion.DELETE_CLAIMS_PERMISSION
import party.morino.moripaactivityreport.paper.claim.ClaimDeletionListener.Companion.DELETE_CLAIM_COMMANDS
import party.morino.moripaactivityreport.paper.ticket.TicketSuggester

/**
 * GriefPrevention の /deleteclaim の理由に、「close #番号」と「--silent」の Tab 補完を付ける
 *
 * /deleteclaim は GriefPrevention のコマンドで Cloud の補完 (TooltipSuggestion) を付けられないため、
 * Paper の [AsyncTabCompleteEvent] で候補と tooltip を返す。
 */
class DeleteClaimTabCompleter : Listener, KoinComponent {
    private val ticketSuggester: TicketSuggester by inject()

    /**
     * /deleteclaim の理由を補完する
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onTabComplete(event: AsyncTabCompleteEvent) {
        if (!event.isCommand || event.isHandled) return
        val player = event.sender as? Player ?: return
        // 先頭の / を除き、コマンド名と引数に分ける (引数を打ち始めていない場合は補完しない)
        val parts = event.buffer.removePrefix("/").split(' ', limit = 2)
        if (parts.size < 2 || parts.first().lowercase() !in DELETE_CLAIM_COMMANDS) return
        if (!player.hasPermission(DELETE_CLAIMS_PERMISSION)) return

        val suggestions = DeleteClaimSuggestions.suggest(parts[1]) {
            // チケットの取得はデータベースを待つため、非同期で呼ばれた場合だけ行う
            if (event.isAsynchronous) ticketSuggester.openTickets(player) else emptyList()
        }
        // 候補が無い場合は、プレイヤー名などの既定の補完に任せる
        if (suggestions.isEmpty()) return
        event.completions(
            suggestions.map { suggestion ->
                AsyncTabCompleteEvent.Completion.completion(suggestion.text, suggestion.tooltip?.let(Component::text))
            },
        )
        event.isHandled = true
    }
}
