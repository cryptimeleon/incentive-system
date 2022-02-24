package org.cryptimeleon.incentive.app.domain.model

import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata
import org.cryptimeleon.math.structures.cartesian.Vector
import java.math.BigInteger

/**
 * Choices that the user is currently eligible for.
 */
sealed class UpdateChoice() {
    object None : UpdateChoice() {
        override fun toUserUpdateChoice() = org.cryptimeleon.incentive.app.domain.model.None
        override fun toString() = "No Update"
    }

    data class Earn(val pointsToEarn: Vector<BigInteger>) : UpdateChoice() {
        override fun toUserUpdateChoice() = Earn
        override fun toString() = "Fast Earn of ${pointsToEarn.print()} points"
    }

    data class ZKP(
        val update: ZkpTokenUpdate,
        val oldPoints: Vector<BigInteger>,
        val newPoints: Vector<BigInteger>,
        val metadata: ZkpTokenUpdateMetadata
    ) :
        UpdateChoice() {
        override fun toUserUpdateChoice() = ZKP(update.tokenUpdateId)
        override fun toString() =
            "ZKP '${update.rewardDescription}'\n${oldPoints.print()} -> ${newPoints.print()}"
    }

    abstract fun toUserUpdateChoice(): UserUpdateChoice
}

private fun Vector<BigInteger>.print(): String =
    "[${this.stream().map { x: BigInteger -> x.toString() }.reduce { t, u -> "$t $u" }.get()}]"
