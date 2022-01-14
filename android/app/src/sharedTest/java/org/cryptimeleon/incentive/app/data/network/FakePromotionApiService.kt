package org.cryptimeleon.incentive.app.data.network

import org.cryptimeleon.incentive.promotion.Promotion
import org.cryptimeleon.math.serialization.converter.JSONConverter
import retrofit2.Response

class FakePromotionApiService(
    private val promotions: List<Promotion>
) : PromotionApiService {
    private val jsonConverter = JSONConverter()
    override suspend fun getPromotions(): Response<List<String>> =
        Response.success(promotions.map { jsonConverter.serialize(it.representation) })
}
