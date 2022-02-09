package org.cryptimeleon.incentive.app.domain.model

import java.util.*

data class Basket(
    val basketId: UUID,
    val items: List<BasketItem>,
    val paid: Boolean,
    val redeemed: Boolean,
    val value: Int,
)
