package org.cryptimeleon.incentive.app.domain.model

import java.util.*

data class BasketItem(
    val itemId: UUID,
    val title: String,
    val price: Int,
    val count: Int,
)