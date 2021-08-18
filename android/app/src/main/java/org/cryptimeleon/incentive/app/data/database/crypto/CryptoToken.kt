package org.cryptimeleon.incentive.app.data.database.crypto

import org.cryptimeleon.incentive.crypto.model.Token

data class CryptoToken(
    val token: Token,
    val promotionId: Int,
    val cryptoMaterialId: Int,
)
