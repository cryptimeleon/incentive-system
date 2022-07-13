package org.cryptimeleon.incentive.app.domain.model

import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata
import org.cryptimeleon.incentive.promotion.sideeffect.SideEffect
import org.cryptimeleon.math.structures.cartesian.Vector
import java.math.BigInteger
import java.util.*

/**
 * Choices that the user is currently eligible for.
 */
sealed class UpdateChoice() {
    object None : UpdateChoice() {
        override fun toUserUpdateChoice() =
            SerializableUserChoice.None

        override fun toString() = "No Update"
    }

    data class Earn(val pointsToEarn: Vector<BigInteger>) : UpdateChoice() {
        override fun toUserUpdateChoice() = SerializableUserChoice.Earn
        override fun toString() = "Fast Earn of ${pointsToEarn.print()} points"
    }

    data class ZKP(
        val updateId: UUID,
        val updateDescription: String,
        val oldPoints: Vector<BigInteger>,
        val newPoints: Vector<BigInteger>,
        val metadata: ZkpTokenUpdateMetadata,
        val sideEffect: SideEffect
    ) :
        UpdateChoice() {
        override fun toUserUpdateChoice() = SerializableUserChoice.ZKP(updateId)
        override fun toString() =
            "ZKP '${updateDescription}'\n${oldPoints.print()} -> ${newPoints.print()}"
    }

    abstract fun toUserUpdateChoice(): SerializableUserChoice.UserUpdateChoice
}

private fun Vector<BigInteger>.print(): String =
    "[${this.stream().map { x: BigInteger -> x.toString() }.reduce { t, u -> "$t $u" }.get()}]"
