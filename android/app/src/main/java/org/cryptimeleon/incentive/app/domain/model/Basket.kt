package org.cryptimeleon.incentive.app.domain.model

import java.util.*

data class Basket(
    val basketId: UUID,
    val items: List<BasketItem>,
    val paid: Boolean,
    val value: Int,
) {
    fun toPromotionBasket(): org.cryptimeleon.incentive.promotion.model.Basket {
        return org.cryptimeleon.incentive.promotion.model.Basket(
            basketId,
            items.map { it.toPromotionBasketItem() }
        )
    }
}
