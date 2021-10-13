package org.cryptimeleon.incentive.promotion;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ExplicitPromotionManagerTest {

    String HAZELNUT_SPREAD = "Chocolate Hazelnut Spread";
    String HAZELNUT_SPREAD_ID = "id-0";
    String APPLE = "Apple";
    String APPLE_ID = "id-1";
    String TOOTHBRUSH = "Toothbrush";
    String TOOTHBRUSH_ID = "id-2";

    String FREE_TEDDY = "Teddy Bear";
    String FREE_PAN = "Pan";
    String FREE_HAZELNUT_SPREAD = "Free Chocolate Hazelnut Spread";


    PromotionBasket emptyPromotionBasket = new PromotionBasket(Collections.emptyList());
    PromotionBasket promotionBasket = new PromotionBasket(List.of(
            new BasketItem(new Item(APPLE, APPLE_ID, 199), 3),
            new BasketItem(new Item(TOOTHBRUSH, TOOTHBRUSH_ID, 199), 8),
            new BasketItem(new Item(HAZELNUT_SPREAD, HAZELNUT_SPREAD_ID, 199), 8)
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
            Map.of(APPLE_ID, 5L, TOOTHBRUSH_ID, 10L
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
        var points = PromotionManager.computePoints(List.of(firstPromotion, secondPromotion), promotionBasket);
        assert points.stream().anyMatch(promotionPoints -> promotionPoints.getPromotionId() == firstPromotion.getPromotionId() && promotionPoints.getPoints() == 95);
        assert points.stream().anyMatch(promotionPoints -> promotionPoints.getPromotionId() == secondPromotion.getPromotionId() && promotionPoints.getPoints() == 8);
    }

    @Test
    void testPointsToEarnEmptyBasket() {
        var points = PromotionManager.computePoints(List.of(firstPromotion, secondPromotion), emptyPromotionBasket);
        assert points.stream().anyMatch(promotionPoints -> promotionPoints.getPromotionId() == firstPromotion.getPromotionId() && promotionPoints.getPoints() == 0);
        assert points.stream().anyMatch(promotionPoints -> promotionPoints.getPromotionId() == secondPromotion.getPromotionId() && promotionPoints.getPoints() == 0);
    }
}
