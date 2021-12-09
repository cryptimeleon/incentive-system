package org.cryptimeleon.incentive.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.cryptimeleon.incentive.app.data.BaseCryptoRepositoryTest
import org.cryptimeleon.incentive.app.data.CryptoRepository
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoDao
import org.cryptimeleon.incentive.app.data.database.crypto.CryptoDatabase
import org.cryptimeleon.incentive.app.data.network.FakeCryptoApiService
import org.cryptimeleon.incentive.app.data.network.FakeInfoApiService

class CryptoRepositoryTest : BaseCryptoRepositoryTest() {

    private lateinit var cryptoDao: CryptoDao
    private lateinit var db: CryptoDatabase

    private val fakeCryptoApiService =
        FakeCryptoApiService(pp, pkp, listOf(firstPromotionParameters, secondPromotionParameters))
    private val fakeInfoApiService = FakeInfoApiService(pp, pkp.pk)

    override fun before() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, CryptoDatabase::class.java
        ).build()
        cryptoDao = db.cryptoDatabaseDao()
        cryptoRepository =
            CryptoRepository(fakeInfoApiService, fakeCryptoApiService, cryptoDao)
    }

    override fun after() {
        db.close()
    }
}