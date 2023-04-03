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
import okhttp3.logging.HttpLoggingInterceptor
import org.cryptimeleon.incentive.app.BuildConfig
import org.cryptimeleon.incentive.app.data.*
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
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


private const val USER_PREFERENCES = "user_preferences"

// This is a placeholder that is replaced by the StoreInterceptor to keep only the path segments
private const val BASKET_URL_PLACEHOLDER = "https://basketservice.org"

@Module
@InstallIn(SingletonComponent::class)
class HiltApiModule {

    @Singleton
    @Provides
    fun provideStoreInterceptor(storeSelectionRepository: StoreSelectionRepository) =
        StoreInterceptor(storeSelectionRepository)


    @Provides
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Singleton
    @Provides
    fun provideStoreApiService(storeInterceptor: StoreInterceptor): StoreApiService {
        val loggingInterceptor = HttpLoggingInterceptor { Timber.tag("OkHttp").d(it) }
        loggingInterceptor.apply { loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(storeInterceptor)
            .build()

        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASKET_URL_PLACEHOLDER)
            .build()
            .create(StoreApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideBasketApiService(storeInterceptor: StoreInterceptor): BasketApiService {
        val loggingInterceptor = HttpLoggingInterceptor { Timber.tag("OkHttp").d(it) }
        loggingInterceptor.apply { loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(storeInterceptor)
            .build()

        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASKET_URL_PLACEHOLDER)
            .build()
            .create(BasketApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideInfoApiService(): InfoApiService =
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(BuildConfig.INFO_SERVICE_URL)
            .build()
            .create(InfoApiService::class.java)

    @Singleton
    @Provides
    fun providePromotionApiService(): PromotionApiService =
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.PROMOTION_SERVICE_URL)
            .build()
            .create(PromotionApiService::class.java)

    @Singleton
    @Provides
    fun provideCryptoApiService(): ProviderApiService {
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
            .baseUrl(BuildConfig.PROMOTION_SERVICE_URL)
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
    fun providePreferencesDataStore(@ApplicationContext appContext: Context) =
        PreferenceDataStoreFactory.create {
            appContext.preferencesDataStoreFile(USER_PREFERENCES)
        }

    @Singleton
    @Provides
    fun providePreferencesRepository(dataStore: DataStore<Preferences>): IPreferencesRepository =
        PreferencesRepository(dataStore)

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
    fun providerStoreSelectionRepository(
        preferencesRepository: IPreferencesRepository
    ): StoreSelectionRepository = StoreSelectionRepository(preferencesRepository)

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
}
