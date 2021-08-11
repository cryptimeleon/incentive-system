package org.cryptimeleon.incentive.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.cryptimeleon.incentive.app.network.BasketApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


private const val BASKET_BASE_URL = "https://incentives.cs.upb.de/basket/"

@Module
@InstallIn(SingletonComponent::class)
class HiltModules {

    @Singleton
    @Provides
    fun provideBasketApiService(): BasketApiService =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASKET_BASE_URL)
            .build()
            .create(BasketApiService::class.java)
}