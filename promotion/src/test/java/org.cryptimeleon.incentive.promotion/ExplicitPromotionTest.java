package org.cryptimeleon.incentive.promotion;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExplicitPromotionTest {

    String HAZELNUT_SPREAD = "Chocolate Hazelnut Spread";
    String HAZELNUT_SPREAD_ID = "id-0";
    String APPLE = "Apple";
    String APPLE_ID = "id-1";
    String TOOTHBRUSH = "Toothbrush";
    String TOOTHBRUSH_ID = "id-2";

    String FREE_TEDDY = "Teddy Bear";
    String FREE_PAN = "Pan";
    String FREE_HAZELNUT_SPREAD = "Free Chocolate Hazelnut Spread";


    Basket basket = new Basket(Map.of(
            new BasketItem(APPLE, APPLE_ID, 199), 3L,
            new BasketItem(TOOTHBRUSH, TOOTHBRUSH_ID, 199), 8L,
            new BasketItem(HAZELNUT_SPREAD, HAZELNUT_SPREAD_ID, 199), 8L
    ));

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

    PromotionDescription secondPromotion = new ExplicitPromotionDescription(
            8,
            "Another classic Promotion",
            "Get every fifth jar of hazelnut spread for free",
            LocalDate.of(2021, 1, 1),
            LocalDate.of(2021, 12, 31),
            List.of(
                    new PromotionReward(4L, FREE_HAZELNUT_SPREAD)
            ),
            Map.of(
                    HAZELNUT_SPREAD_ID, 1L
            )
    );

    @Test
    void testPointsToEarn() {
        var points = Promotion.computePoints(List.of(firstPromotion, secondPromotion), basket);
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

    @Test
    void testSecondPromotionRewards() {
        var rewards = Promotion.qualifiedRewards(secondPromotion, 3);
        assertEquals(rewards.size(), 0);

        rewards = Promotion.qualifiedRewards(secondPromotion, 4);
        assert rewards.size() == 1;
        assert rewards.stream().anyMatch(promotionReward -> promotionReward.getRewardTitle().equals(FREE_HAZELNUT_SPREAD));
    }

    @Test
    void testTimePeriod() {
        // Check valid dates
        assertTrue(secondPromotion.isValidAt(LocalDate.of(2021, 1, 1)));
        assertTrue(secondPromotion.isValidAt(LocalDate.of(2021, 5, 10)));
        assertTrue(secondPromotion.isValidAt(LocalDate.of(2021, 12, 31)));

        // Check invalid dates
        assertFalse(secondPromotion.isValidAt(LocalDate.of(2020, 5, 10)));
        assertFalse(secondPromotion.isValidAt(LocalDate.of(2022, 12, 31)));
    }
}
