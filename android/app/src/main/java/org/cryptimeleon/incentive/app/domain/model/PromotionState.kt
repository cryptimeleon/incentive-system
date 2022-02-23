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
sealed class UpdateChoice {
    object None : UpdateChoice()
    data class Earn(val pointsToEarn: Vector<BigInteger>) : UpdateChoice()
    data class ZKP(val update: ZkpTokenUpdate, val metadata: ZkpTokenUpdateMetadata) :
        UpdateChoice()
}
