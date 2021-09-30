package org.cryptimeleon.incentive.promotion;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProportionalPromotionTest {
    String HAZELNUT_SPREAD = "Chocolate Hazelnut Spread";
    String HAZELNUT_SPREAD_ID = "id-0";
    String APPLE = "Apple";
    String APPLE_ID = "id-1";
    String TOOTHBRUSH = "Toothbrush";
    String TOOTHBRUSH_ID = "id-2";


    Basket basket = new Basket(List.of(
            new BasketItem(new Item(APPLE, APPLE_ID, 10), 3),
            new BasketItem(new Item(TOOTHBRUSH, TOOTHBRUSH_ID, 50), 8),
            new BasketItem(new Item(HAZELNUT_SPREAD, HAZELNUT_SPREAD_ID, 200), 8)
    ));

    Basket emptyBasket = new Basket(Collections.emptyList());

    ProportionalPromotionDescription promotionDescription = new ProportionalPromotionDescription(
            7L,
            "Test Promotion",
            "Earn 10 points for every cent spent",
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2021, 1, 1),
            10,
            List.of()
    );

    @Test
    void emptyBasketTest() {
        var points = Promotion.computePoints(List.of(promotionDescription), emptyBasket);
        assertEquals(1, points.size());
        assertEquals(0, points.get(0).getPoints());
    }

    @Test
    void proportionalPromotionTest() {
        var points = Promotion.computePoints(List.of(promotionDescription), basket);
        assertEquals(1, points.size());
        assertEquals(20300, points.get(0).getPoints());
    }
}
