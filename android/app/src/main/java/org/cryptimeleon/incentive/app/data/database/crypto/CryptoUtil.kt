package org.cryptimeleon.incentive.app.data.database.crypto

import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.Token
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey
import org.cryptimeleon.math.serialization.converter.JSONConverter

/**
 * Utility for serializing tokens to database objects and vice versa.
 * This is not a Converter since it requires the public parameters as additional resources.
 */
object CryptoUtil {
    private val jsonConverter = JSONConverter()

    fun fromCryptoToken(cryptoToken: CryptoToken): SerializedCryptoToken = SerializedCryptoToken(
        jsonConverter.serialize(cryptoToken.token.representation),
        cryptoToken.cryptoMaterialId, cryptoToken.promotionId
    )

    fun toCryptoToken(
        serializedCryptoToken: SerializedCryptoToken,
        pp: IncentivePublicParameters
    ): CryptoToken =
        CryptoToken(
            Token(jsonConverter.deserialize(serializedCryptoToken.serializedToken), pp),
            serializedCryptoToken.promotionId,
            serializedCryptoToken.promotionId
        )

    fun fromSerializedCryptoAsset(serializedCryptoMaterial: SerializedCryptoMaterial): CryptoMaterial {
        val pp =
            IncentivePublicParameters(jsonConverter.deserialize(serializedCryptoMaterial.serializedPublicParameters))
        val ppk = ProviderPublicKey(
            jsonConverter.deserialize(serializedCryptoMaterial.serializedProviderPublicKey),
            pp.spsEq,
            pp.bg.g1
        )
        val upk = UserPublicKey(
            jsonConverter.deserialize(serializedCryptoMaterial.serializedUserPublicKey),
            pp.bg.g1
        )
        val usk = UserSecretKey(
            jsonConverter.deserialize(serializedCryptoMaterial.serializedUserSecretKey),
            pp.bg.zn,
            pp.prfToZn
        )
        return CryptoMaterial(
            pp,
            ppk,
            UserKeyPair(upk, usk),
            IncentiveSystem(pp),
            serializedCryptoMaterial.id
        )
    }

    fun toSerializedCryptoAsset(cryptoMaterial: CryptoMaterial): SerializedCryptoMaterial =
        SerializedCryptoMaterial(
            jsonConverter.serialize(cryptoMaterial.pp.representation),
            jsonConverter.serialize(cryptoMaterial.ppk.representation),
            jsonConverter.serialize(cryptoMaterial.ukp.pk.representation),
            jsonConverter.serialize(cryptoMaterial.ukp.sk.representation),
            cryptoMaterial.id,
        )
}