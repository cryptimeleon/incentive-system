package org.cryptimeleon.incentive.app.data.database.crypto

import org.cryptimeleon.incentive.crypto.IncentiveSystem
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
    val incentiveSystem: IncentiveSystem,
    val id: Int
)
