package org.cryptimeleon.incentive.app.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.cryptimeleon.incentive.app.domain.IPromotionRepository
import org.cryptimeleon.incentive.promotion.Promotion
import org.cryptimeleon.incentive.promotion.Reward
import org.cryptimeleon.incentive.promotion.RewardSideEffect
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion
import org.cryptimeleon.incentive.promotion.hazel.HazelReward
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

abstract class BasePromotionRepositoryTest {

    lateinit var promotionRepository: IPromotionRepository

    abstract fun before()
    abstract fun after()

    val promotions = listOf<Promotion>(
        HazelPromotion(
            HazelPromotion.generatePromotionParameters(),
            "First test promotion",
            "Description of first test promotion",
            listOf<Reward>(
                HazelReward(
                    3,
                    "Earn a free teddy bear!",
                    UUID.randomUUID(),
                    RewardSideEffect("Free Teddy")
                )
            ),
            "Hazel"
        ),
        HazelPromotion(
            HazelPromotion.generatePromotionParameters(),
            "Second test promotion",
            "Description of second test promotion",
            listOf<Reward>(
                HazelReward(
                    7,
                    "Earn a free pan!",
                    UUID.randomUUID(),
                    RewardSideEffect("Free Pan")
                )
            ),
            "Hazel"
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
