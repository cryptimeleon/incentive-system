package org.cryptimeleon.incentive.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.app.data.database.basket.BasketDatabase
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoDatabase
import org.cryptimeleon.incentive.app.data.network.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

private const val BASKET_BASE_URL = "https://incentives.cs.upb.de/basket/"
private const val INFO_BASE_URL = "https://incentives.cs.upb.de/info/"
private const val ISSUE_BASE_URL = "https://incentives.cs.upb.de/issue/"
private const val CREDIT_BASE_URL = "https://incentives.cs.upb.de/credit/"
private const val PROMOTION_BASE_URL = "https://incentives.cs.upb.de/promotion/"

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

    @Singleton
    @Provides
    fun provideIssueJoinApiService(): IssueJoinApiService =
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(ISSUE_BASE_URL)
            .build()
            .create(IssueJoinApiService::class.java)

    @Singleton
    @Provides
    fun providePromotionApiService(): PromotionApiService =
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(PROMOTION_BASE_URL)
            .build()
            .create(PromotionApiService::class.java)
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
}

@Module
@InstallIn(SingletonComponent::class)
class HiltRepositoryModule {

    @Singleton
    @Provides
    fun provideCryptoRepository(
        creditEarnApiService: CreditEarnApiService,
        infoApiService: InfoApiService,
        issueJoinApiService: IssueJoinApiService,
        cryptoDatabase: CryptoDatabase,
    ): CryptoRepository =
        CryptoRepository(
            creditEarnApiService,
            infoApiService,
            issueJoinApiService,
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
            basketDatabase.basketDatabaseDao()
        )

    @Singleton
    @Provides
    fun providePromotionRepository(
        promotionApiService: PromotionApiService,
    ): PromotionRepository =
        PromotionRepository(
            promotionApiService
        )
}
