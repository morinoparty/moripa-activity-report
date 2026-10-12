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
package party.morino.moripaactivityreport.paper.model.claim

import java.util.UUID

/**
 * 削除された土地保護 (claim) のうち、報告に使う情報
 *
 * GriefPrevention の Claim は削除後に状態が変わりうるため、削除を検知した時点で必要な値を写し取っておく。
 *
 * @property ownerName 持ち主の名前 (運営の土地保護の場合は GriefPrevention が返す表示名)
 * @property ownerUuid 持ち主の UUID (運営の土地保護の場合は null)
 * @property x 土地保護の北西の角の X 座標
 * @property z 土地保護の北西の角の Z 座標
 */
data class DeletedClaim(
    val ownerName: String,
    val ownerUuid: UUID?,
    val x: Int,
    val z: Int,
)
