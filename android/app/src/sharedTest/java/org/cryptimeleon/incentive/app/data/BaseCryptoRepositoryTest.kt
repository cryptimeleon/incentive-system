package org.cryptimeleon.incentive.app.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.cryptimeleon.incentive.app.domain.ICryptoRepository
import org.cryptimeleon.incentive.crypto.BilinearGroupChoice
import org.cryptimeleon.incentive.crypto.IncentiveSystem
import org.cryptimeleon.incentive.crypto.Util
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters
import org.cryptimeleon.incentive.crypto.model.PromotionParameters
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair
import org.junit.After
import org.junit.Before
import org.junit.Test

abstract class BaseCryptoRepositoryTest {

    val pp: IncentivePublicParameters = IncentiveSystem.setup(128, BilinearGroupChoice.Debug)
    val incentiveSystem = IncentiveSystem(pp)
    val pkp: ProviderKeyPair = incentiveSystem.generateProviderKeyPair()
    val upkp = incentiveSystem.generateUserPreKeyPair()
    val ukp: UserKeyPair = Util.addGenesisSignatureToUserKeys(upkp, pkp, pp)
    val userName = "TestUser"

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
        cryptoRepository.refreshCryptoMaterial(userName)
        assertThat(cryptoRepository.cryptoMaterial.first()).isNotNull()
    }

    @Test
    fun testTokensAndIssueJoin() = runBlocking {
        cryptoRepository.refreshCryptoMaterial(userName)

        assertThat(cryptoRepository.tokens.first()).isEmpty()
        cryptoRepository.runIssueJoin(firstPromotionParameters)
        val tokensAfterInsert = cryptoRepository.tokens.first()
        assertThat(tokensAfterInsert).hasSize(1)
    }

    @Test
    fun testTokensAndIssueJoinReplaceIfPresent() = runBlocking {
        cryptoRepository.refreshCryptoMaterial(userName)

        cryptoRepository.runIssueJoin(firstPromotionParameters)
        val tokensAfterInsert = cryptoRepository.tokens.first()
        cryptoRepository.runIssueJoin(firstPromotionParameters, replaceIfPresent = true)

        val tokensAfterNonReplacingInsert = cryptoRepository.tokens.first()
        assertThat(tokensAfterNonReplacingInsert).containsNoneIn(tokensAfterInsert)
    }

    @Test
    fun testTokensAndIssueJoinDoNotReplaceIfPresent(): Unit = runBlocking {
        cryptoRepository.refreshCryptoMaterial(userName)

        cryptoRepository.runIssueJoin(firstPromotionParameters)
        val tokensAfterInsert = cryptoRepository.tokens.first()
        cryptoRepository.runIssueJoin(firstPromotionParameters, replaceIfPresent = false)

        val tokensAfterNonReplacingInsert = cryptoRepository.tokens.first()
        assertThat(tokensAfterNonReplacingInsert).containsExactlyElementsIn(tokensAfterInsert)
    }

    @Test
    fun testMultiplePromotionsAreKeptSeparately() = runBlocking {
        cryptoRepository.refreshCryptoMaterial(userName)

        cryptoRepository.runIssueJoin(firstPromotionParameters)
        cryptoRepository.runIssueJoin(secondPromotionParameters)

        assertThat(cryptoRepository.tokens.first()).hasSize(2)
    }
}
