package org.cryptimeleon.incentive.app.data.network

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey
import org.cryptimeleon.math.serialization.converter.JSONConverter
import retrofit2.Response

class FakeInfoApiService(
    private val pp: IncentivePublicParameters,
    private val providerPublicKey: ProviderPublicKey
) : InfoApiService {
    private val jsonConverter = JSONConverter()

    override suspend fun getPublicParameters(): Response<String> {
        return Response.success(jsonConverter.serialize(pp.representation))
    }

    override suspend fun getProviderPublicKey(): Response<String> {
        return Response.success(jsonConverter.serialize(providerPublicKey.representation))
    }
}