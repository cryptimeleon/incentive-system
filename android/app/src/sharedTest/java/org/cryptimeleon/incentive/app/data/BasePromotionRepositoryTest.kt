package org.cryptimeleon.incentive.app.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.promotion.promotions.NutellaPromotion
import org.cryptimeleon.incentive.promotion.promotions.Promotion
import org.cryptimeleon.incentive.promotion.reward.NutellaReward
import org.cryptimeleon.incentive.promotion.reward.Reward
import org.cryptimeleon.incentive.promotion.reward.RewardSideEffect
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

abstract class BasePromotionRepositoryTest {

    lateinit var promotionRepository: IPromotionRepository

    abstract fun before(): Unit
    abstract fun after(): Unit

    val promotions = listOf<Promotion>(
        NutellaPromotion(
            NutellaPromotion.generatePromotionParameters(),
            "First test promotion",
            "Description of first test promotion",
            listOf<Reward>(
                NutellaReward(
                    3,
                    "Earn a free teddy bear!",
                    UUID.randomUUID(),
                    RewardSideEffect("Free Teddy")
                )
            ),
        "nutella"
        ),
        NutellaPromotion(
            NutellaPromotion.generatePromotionParameters(),
            "Second test promotion",
            "Description of second test promotion",
            listOf<Reward>(
                NutellaReward(
                    7,
                    "Earn a free pan!",
                    UUID.randomUUID(),
                    RewardSideEffect("Free Pan")
                )
            ),
            "nutella"
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
