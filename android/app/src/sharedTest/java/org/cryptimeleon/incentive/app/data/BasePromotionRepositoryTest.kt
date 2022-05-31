package org.cryptimeleon.incentive.app.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect
import org.cryptimeleon.incentive.promotion.vip.VipPromotion
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

abstract class BasePromotionRepositoryTest {

    lateinit var promotionRepository: IPromotionRepository

    abstract fun before()
    abstract fun after()

    val promotions = listOf(
        HazelPromotion(
            HazelPromotion.generatePromotionParameters(),
            "First test promotion",
            "Description of first test promotion",
            listOf<ZkpTokenUpdate>(
                HazelTokenUpdate(
                    UUID.randomUUID(),
                    "Earn a free teddy bear!",
                    RewardSideEffect("Free Teddy"),
                    3
                )
            ),
            "Hazel"
        ),
        VipPromotion(
            VipPromotion.generatePromotionParameters(),
            "Second test promotion",
            "A VIP style promotion",
            5,
            10,
            20,
            RewardSideEffect("Bronze Advantages"),
            RewardSideEffect("Silver Advantages"),
            RewardSideEffect("Gold Advantages")
        )
    )

    @Before
    fun setUp() {
        before()
    }

    @After
    fun tearDown() {
        after()
    }

    @Test
    fun testPromotions() = runBlocking {
        val promotionFlow = promotionRepository.promotions
        assertThat(promotionFlow.first()).isEmpty()
        promotionRepository.reloadPromotions()
        assertThat(promotionFlow.first()).isNotEmpty()
    }
}
