package org.cryptimeleon.incentive.app.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.data.BasketRepository
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.PreferencesRepository
import org.cryptimeleon.incentive.app.data.PromotionRepository
import org.cryptimeleon.incentive.app.data.database.basket.BasketDatabase
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoDatabase
import org.cryptimeleon.incentive.app.data.database.promotion.PromotionDatabase
import org.cryptimeleon.incentive.app.data.network.*
import org.cryptimeleon.incentive.app.domain.IBasketRepository
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton



private const val USER_PREFERENCES = "user_preferences"

data class UrlConfig(
    val basket_url: String,
    val info_url: String,
    val promotion_url: String
)


@Module
@InstallIn(SingletonComponent::class)
class HiltApiModule {

    @Provides
    fun provideUrls(@ApplicationContext context: Context): UrlConfig =
        UrlConfig(
            basket_url = context.getString(R.string.basket_service_url),
            info_url = context.getString(R.string.info_service_url),
            promotion_url = context.getString(R.string.promotion_service_url),
        )

    @Provides
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Singleton
    @Provides
    fun provideStoreApiService(urlConfig: UrlConfig): StoreApiService {
        return Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(urlConfig.basket_url)
            .build()
            .create(StoreApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideBasketApiService(urlConfig: UrlConfig): BasketApiService {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(urlConfig.basket_url)
            .build()
            .create(BasketApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideInfoApiService(urlConfig: UrlConfig): InfoApiService =
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(urlConfig.info_url)
            .build()
            .create(InfoApiService::class.java)

    @Singleton
    @Provides
    fun providePromotionApiService(urlConfig: UrlConfig): PromotionApiService =
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(urlConfig.promotion_url)
            .build()
            .create(PromotionApiService::class.java)

    @Singleton
    @Provides
    fun provideDosApiService(urlConfig: UrlConfig): DosApiService =
        Retrofit.Builder()
            .baseUrl(urlConfig.promotion_url)
            .build()
            .create(DosApiService::class.java)

    @Singleton
    @Provides
    fun provideCryptoApiService(urlConfig: UrlConfig): ProviderApiService {
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
            .baseUrl(urlConfig.promotion_url)
            .build()
            .create(ProviderApiService::class.java)
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
        providerApiService: ProviderApiService,
        cryptoDatabase: CryptoDatabase,
        storeApiService: StoreApiService,
    ): ICryptoRepository =
        CryptoRepository(
            infoApiService,
            providerApiService,
            cryptoDatabase.cryptoDatabaseDao(),
            storeApiService,
        )

    @Singleton
    @Provides
    fun provideBasketRepository(
        basketApiService: BasketApiService,
        basketDatabase: BasketDatabase,
    ): IBasketRepository =
        BasketRepository(
            basketApiService,
            basketDatabase.basketDatabaseDao(),
        )

    @Singleton
    @Provides
    fun providePromotionRepository(
        promotionApiService: PromotionApiService,
        promotionDatabase: PromotionDatabase
    ): IPromotionRepository =
        PromotionRepository(
            promotionApiService,
            promotionDatabase.promotionDatabaseDao()
        )

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context) =
        PreferenceDataStoreFactory.create {
            appContext.preferencesDataStoreFile(USER_PREFERENCES)
        }

    @Singleton
    @Provides
    fun providePreferencesRepository(dataSTore: DataStore<Preferences>): IPreferencesRepository =
        PreferencesRepository(dataSTore)
}
