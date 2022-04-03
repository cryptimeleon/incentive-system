package org.cryptimeleon.incentive.app.domain.model

import org.cryptimeleon.incentive.promotion.Promotion
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata
import org.cryptimeleon.math.structures.cartesian.Vector
import java.math.BigInteger
import java.util.*

/**
 * Represents the current user state for a certain promotion.
 */
data class PromotionState(
    val promotionId: BigInteger,
    val promotionName: String,
    val basketPoints: Vector<BigInteger>,
    val tokenPoints: Vector<BigInteger>,
    val qualifiedUpdates: List<UpdateChoice>
)
