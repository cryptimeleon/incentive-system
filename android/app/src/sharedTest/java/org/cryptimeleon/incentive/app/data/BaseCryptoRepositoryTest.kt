package org.cryptimeleon.incentive.app.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.Setup
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.PromotionParameters
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair
import org.junit.After
import org.junit.Before
import org.junit.Test

abstract class BaseCryptoRepositoryTest {

    val pp: IncentivePublicParameters = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug)
    val incentiveSystem = IncentiveSystem(pp)
    val pkp: ProviderKeyPair = incentiveSystem.generateProviderKeys()

    lateinit var cryptoRepository: ICryptoRepository

    // Setup
    val firstPromotionParameters: PromotionParameters =
        IncentiveSystem.generatePromotionParameters(3)
    val secondPromotionParameters: PromotionParameters =
        IncentiveSystem.generatePromotionParameters(2)

    abstract fun before()
    abstract fun after()

    @Before
    fun setUp() {
        before()
    }

    @After
    fun tearDown() {
        after()
    }

    @Test
    fun testCryptoAssets() = runBlocking {
        assertThat(cryptoRepository.cryptoMaterial.first()).isNull()
        cryptoRepository.refreshCryptoMaterial()
        assertThat(cryptoRepository.cryptoMaterial.first()).isNotNull()
    }

    @Test
    fun testTokensAndIssueJoin() = runBlocking {
        // Fetch pp
        cryptoRepository.refreshCryptoMaterial()

        assertThat(cryptoRepository.tokens.first()).isEmpty()
        cryptoRepository.runIssueJoin(firstPromotionParameters, dummy = true)
        assertThat(cryptoRepository.tokens.first()).isEmpty()
        cryptoRepository.runIssueJoin(firstPromotionParameters, dummy = false)
        assertThat(cryptoRepository.tokens.first()).hasSize(1)
        cryptoRepository.runIssueJoin(secondPromotionParameters, dummy = false)
        assertThat(cryptoRepository.tokens.first()).hasSize(2)
        cryptoRepository.runIssueJoin(firstPromotionParameters, dummy = false)
        assertThat(cryptoRepository.tokens.first()).hasSize(2)
    }
}
