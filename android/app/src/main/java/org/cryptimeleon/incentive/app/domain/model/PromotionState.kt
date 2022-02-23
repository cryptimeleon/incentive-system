package org.cryptimeleon.incentive.app.domain.model

import org.cryptimeleon.incentive.promotion.Promotion
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata
import org.cryptimeleon.math.structures.cartesian.Vector
import java.math.BigInteger

/**
 * Represents the current user state for a certain promotion.
 */
data class PromotionState(
    val promotion: Promotion,
    val basketPoints: Vector<BigInteger>,
    val tokenPoints: Vector<BigInteger>,
    val qualifiedUpdates: List<UpdateChoice>
)

/**
 * Choices that the user is currently eligible for.
 */
sealed class UpdateChoice() {
    object None : UpdateChoice() {
        override fun toString(): String {
            return "No Update"
        }
    }

    data class Earn(val pointsToEarn: Vector<BigInteger>) : UpdateChoice() {
        override fun toString(): String {
            return "Fast Earn of ${pointsToEarn.print()} points"
        }
    }

    data class ZKP(
        val update: ZkpTokenUpdate,
        val oldPoints: Vector<BigInteger>,
        val newPoints: Vector<BigInteger>,
        val metadata: ZkpTokenUpdateMetadata
    ) :
        UpdateChoice() {
        override fun toString(): String {
            return "ZKP '${update.rewardDescription}'\n${oldPoints.print()} -> ${newPoints.print()}"
        }
    }
}

private fun Vector<BigInteger>.print(): String =
    "[${this.stream().map { x: BigInteger -> x.toString() }.reduce { t, u -> "$t $u" }.get()}]"
