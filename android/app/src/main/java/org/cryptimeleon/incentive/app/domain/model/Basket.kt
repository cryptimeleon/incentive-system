package org.cryptimeleon.incentive.app.domain.model

import java.util.*


data class Basket(
    val items: List<BasketItem>,
    val value: Int,
) {
    // TODO this default argument is a little hacky, but the id is only available once the basket is paid
    fun toPromotionBasket(basketId: UUID = UUID.fromString("123e4567-e89b-42d3-a456-556642440000")): org.cryptimeleon.incentive.promotion.model.Basket {
        return org.cryptimeleon.incentive.promotion.model.Basket(
            basketId,
            items.map { it.toPromotionBasketItem() }
        )
    }
}
