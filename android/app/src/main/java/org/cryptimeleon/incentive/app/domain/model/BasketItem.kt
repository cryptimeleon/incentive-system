package org.cryptimeleon.incentive.app.domain.model

data class BasketItem(
    val itemId: String,
    val title: String,
    val price: Int,
    val count: Int,
)