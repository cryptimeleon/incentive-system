package org.cryptimeleon.incentive.app.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.app.data.database.basket.BasketDatabase
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoDatabase
import org.cryptimeleon.incentive.app.data.database.promotion.PromotionDatabase
import org.cryptimeleon.incentive.app.data.network.BasketApiService
import org.cryptimeleon.incentive.app.data.network.CryptoApiService
import org.cryptimeleon.incentive.app.data.network.InfoApiService
import org.cryptimeleon.incentive.app.data.network.PromotionApiService
import org.cryptimeleon.incentive.app.util.NetworkMonitor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


private const val BASKET_BASE_URL = "https://incentives.cs.upb.de/basket/"
private const val INFO_BASE_URL = "https://incentives.cs.upb.de/info/"
private const val PROMOTION_BASE_URL = "https://incentives.cs.upb.de/promotion/"

@Module
@InstallIn(SingletonComponent::class)
class HiltApiModule {

    @Provides
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    fun provideNetworkMonitor(connectivityManager: ConnectivityManager): NetworkMonitor =
        NetworkMonitor(connectivityManager)

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
    fun provideInfoApiService(): InfoApiService =
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(INFO_BASE_URL)
            .build()
            .create(InfoApiService::class.java)

    @Singleton
    @Provides
    fun providePromotionApiService(): PromotionApiService =
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(PROMOTION_BASE_URL)
            .build()
            .create(PromotionApiService::class.java)

    @Singleton
    @Provides
    fun provideCryptoApiService(): CryptoApiService {
        val okHttpClient =
            OkHttpClient.Builder() // Increase timeouts for batch proofs during development
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(PROMOTION_BASE_URL)
            .build()
            .create(CryptoApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
class HiltDatabaseModule {

    @Singleton
    @Provides
    fun provideCryptoDatabase(@ApplicationContext context: Context): CryptoDatabase =
        Room.databaseBuilder(
            context,
            CryptoDatabase::class.java, "crypto.db"
        ).build()

    @Singleton
    @Provides
    fun provideBasketDatabase(@ApplicationContext context: Context): BasketDatabase =
        Room.databaseBuilder(
            context,
            BasketDatabase::class.java, "basket.db"
        ).build()

    @Singleton
    @Provides
    fun providePromotionDatabase(@ApplicationContext context: Context): PromotionDatabase =
        Room.databaseBuilder(
            context,
            PromotionDatabase::class.java, "promotion.db"
        ).build()
}

@Module
@InstallIn(SingletonComponent::class)
class HiltRepositoryModule {

    @Singleton
    @Provides
    fun provideCryptoRepository(
        infoApiService: InfoApiService,
        cryptoApiService: CryptoApiService,
        cryptoDatabase: CryptoDatabase,
    ): CryptoRepository =
        CryptoRepository(
            infoApiService,
            cryptoApiService,
            cryptoDatabase.cryptoDatabaseDao(),
        )

    @Singleton
    @Provides
    fun provideBasketRepository(
        basketApiService: BasketApiService,
        basketDatabase: BasketDatabase,
    ): BasketRepository =
        BasketRepository(
            basketApiService,
            basketDatabase.basketDatabaseDao(),
        )

    @Singleton
    @Provides
    fun providePromotionRepository(
        promotionApiService: PromotionApiService,
        promotionDatabase: PromotionDatabase
    ): PromotionRepository =
        PromotionRepository(
            promotionApiService,
            promotionDatabase.promotionDatabaseDao()
        )
}
