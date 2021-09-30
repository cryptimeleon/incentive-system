package org.cryptimeleon.incentive.promotion;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PromotionTest {
    String APPLE_ID = "id-1";
    String TOOTHBRUSH_ID = "id-2";

    String FREE_TEDDY = "Teddy Bear";
    String FREE_PAN = "Pan";

    PromotionDescription firstPromotion = new ExplicitPromotionDescription(
            7,
            "Classic Promotion",
            "Earn points for items, get rewards",
            LocalDate.of(2021, 1, 1),
            LocalDate.of(2021, 12, 31),
            List.of(
                    new PromotionReward(20L, FREE_TEDDY),
                    new PromotionReward(100L, FREE_PAN)
            ),
            Map.of(
                    APPLE_ID, 5L,
                    TOOTHBRUSH_ID, 10L
            )
    );

    @Test
    void testTimePeriod() {
        // Check valid dates
        assertTrue(firstPromotion.isValidAt(LocalDate.of(2021, 1, 1)));
        assertTrue(firstPromotion.isValidAt(LocalDate.of(2021, 5, 10)));
        assertTrue(firstPromotion.isValidAt(LocalDate.of(2021, 12, 31)));

        // Check invalid dates
        assertFalse(firstPromotion.isValidAt(LocalDate.of(2020, 5, 10)));
        assertFalse(firstPromotion.isValidAt(LocalDate.of(2022, 12, 31)));
    }

    @Test
    void testFirstPromotionRewards() {
        var rewards = Promotion.qualifiedRewards(firstPromotion, 19);
        assertEquals(rewards.size(), 0);

        rewards = Promotion.qualifiedRewards(firstPromotion, 20);
        assert rewards.size() == 1;
        assert rewards.stream().anyMatch(promotionReward -> promotionReward.getRewardTitle().equals(FREE_TEDDY));


        rewards = Promotion.qualifiedRewards(firstPromotion, 100);
        assert rewards.stream().anyMatch(promotionReward -> promotionReward.getRewardTitle().equals(FREE_PAN));
        assert rewards.stream().anyMatch(promotionReward -> promotionReward.getRewardTitle().equals(FREE_TEDDY));
    }
}
