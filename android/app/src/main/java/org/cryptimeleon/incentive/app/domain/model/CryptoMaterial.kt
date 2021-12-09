package org.cryptimeleon.incentive.app.domain.model

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair

/**
 * Data class holding a set of keys and parameters that are needed to process tokens.
 */
data class CryptoMaterial(
    val pp: IncentivePublicParameters,
    val ppk: ProviderPublicKey,
    val ukp: UserKeyPair,
)
