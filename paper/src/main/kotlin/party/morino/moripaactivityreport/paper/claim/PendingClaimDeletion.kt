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

import me.ryanhamshire.GriefPrevention.Claim
import org.bukkit.entity.Player

/**
 * 実行された /deleteclaim 1 回分の記録
 *
 * コマンドの実行中に発生した削除イベントを集め、次の tick でまとめて報告する。
 *
 * @property player 実行したプレイヤー
 * @property reason 入力された理由 (省略された場合は空文字)
 */
class PendingClaimDeletion(
    val player: Player,
    val reason: String,
) {
    /** コマンドの実行中に削除された土地保護 (親の削除に伴って削除された子も含む) */
    val deletedClaims: MutableList<Claim> = mutableListOf()

    /**
     * 報告の対象にする土地保護を返す
     *
     * 親を削除すると子 (サブディビジョン) の削除イベントも発生するため、一緒に削除された親を持つものは除く。
     *
     * @return コマンドで直接削除された土地保護
     */
    fun rootClaims(): List<Claim> = deletedClaims.filter { claim -> claim.parent == null || claim.parent !in deletedClaims }
}
