package org.cryptimeleon.incentive.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.cryptimeleon.incentive.app.database.crypto.CryptoRepository
import org.cryptimeleon.incentive.app.network.BasketApiService
import org.cryptimeleon.incentive.app.network.CreditEarnApiService
import org.cryptimeleon.incentive.app.network.InfoApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton


private const val BASKET_BASE_URL = "https://incentives.cs.upb.de/basket/"
private const val CREDIT_BASE_URL = "https://incentives.cs.upb.de/credit/"
private const val INFO_BASE_URL = "https://incentives.cs.upb.de/info/"

@Module
@InstallIn(SingletonComponent::class)
class HiltApiModule {

    @Singleton
    @Provides
    fun provideBasketApiService(): BasketApiService =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASKET_BASE_URL)
            .build()
            .create(BasketApiService::class.java)

    @Singleton
    @Provides
    fun provideCreditApiService(): CreditEarnApiService =
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(CREDIT_BASE_URL)
            .build()
            .create(CreditEarnApiService::class.java)

    @Singleton
    @Provides
    fun provideInfoApiService(): InfoApiService =
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(INFO_BASE_URL)
            .build()
            .create(InfoApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
class HiltRepositoryModule {

    @Singleton
    @Provides
    fun provideCryptoRepository(
        creditEarnApiService: CreditEarnApiService,
        @ApplicationContext context: Context
    ): CryptoRepository = CryptoRepository(creditEarnApiService, context)
}
