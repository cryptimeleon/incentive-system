package org.cryptimeleon.incentive.app.domain.model

import org.cryptimeleon.incentive.promotion.model.BasketItem


data class BasketItem(
    val itemId: String,
    val title: String,
    val price: Int,
    val count: Int,
) {
    fun toPromotionBasketItem(): BasketItem {
        return BasketItem(itemId, title, price, count)
    }
}
