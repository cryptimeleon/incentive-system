package org.cryptimeleon.incentive.app.domain.model

import org.cryptimeleon.math.structures.cartesian.Vector
import java.math.BigInteger

/**
 * Represents the current user state for a certain promotion.
 */
data class UserPromotionState(
    val promotionId: BigInteger,
    val promotionName: String,
    val basketPoints: Vector<BigInteger>,
    val tokenPoints: Vector<BigInteger>,
    val qualifiedUpdates: List<UpdateChoice>
)
