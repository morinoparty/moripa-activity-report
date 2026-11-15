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

import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Logger

/**
 * LibertyBans の処罰・解除のイベントが silent (`-s`) で行われたかどうかを調べる
 *
 * LibertyBans 1.1.4 の公開 API (PostPunishEvent / PostPardonEvent) は silent かどうかを持たないが、
 * 実装クラス (PostPunishEventImpl / PostPardonEventImpl) には公開予定の `isSilent()` がある (LibertyBans#350)。
 * そのためリフレクションで呼び出す。LibertyBans 側は `-s` と silent の権限の両方がある場合にだけ true にする。
 *
 * @param logger 調べられなかったことを記録するロガー
 */
class SilentPunishmentDetector(
    private val logger: Logger,
) {
    /** イベントのクラス → `isSilent()` (無い場合は null) */
    private val methods = ConcurrentHashMap<Class<*>, Method?>()

    /** 調べられなかったことを一度だけ記録するための印 */
    private val warned = AtomicBoolean(false)

    /**
     * イベントが silent で行われたかどうかを返す
     *
     * @param event LibertyBans の PostPunishEvent または PostPardonEvent
     * @return silent なら true。調べられない場合 (LibertyBans の実装が変わった場合など) は false (報告する側に倒す)
     */
    fun isSilent(event: Any): Boolean {
        val method = methods.computeIfAbsent(event.javaClass) { findIsSilent(it) }
        val silent = try {
            method?.invoke(event) as? Boolean
        } catch (e: ReflectiveOperationException) {
            null
        }
        if (silent == null) warnOnce(event.javaClass)
        return silent ?: false
    }

    /**
     * `isSilent()` を探す
     *
     * @param type イベントの実装クラス
     * @return 引数なしで boolean を返す公開メソッド (無い場合は null)
     */
    private fun findIsSilent(type: Class<*>): Method? = try {
        type.getMethod(IS_SILENT).takeIf { it.returnType == Boolean::class.javaPrimitiveType }
    } catch (e: NoSuchMethodException) {
        null
    }

    /**
     * silent かどうかを調べられなかったことを、一度だけ記録する
     *
     * @param type イベントの実装クラス
     */
    private fun warnOnce(type: Class<*>) {
        if (warned.compareAndSet(false, true)) {
            logger.warning("Cannot tell whether ${type.name} is silent; silent punishments will be reported to Discord")
        }
    }

    companion object {
        /** LibertyBans のイベントの実装クラスが持つメソッドの名前 */
        private const val IS_SILENT = "isSilent"
    }
}
