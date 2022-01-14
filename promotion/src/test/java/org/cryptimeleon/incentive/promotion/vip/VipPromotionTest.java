package org.cryptimeleon.incentive.promotion.vip;

import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VipPromotionTest {

    /**
     * Test counting items that match a selector.
     */
    @Test
    void testComputeEarningsForBasket() {
        VipPromotion vipPromotion = new VipPromotion(
                VipPromotion.generatePromotionParameters(),
                "Test Promotion",
                "This is a  test promotion",
                1, // Cents to spend for reaching a level
                2,
                3,
                new RewardSideEffect("Bronze Side Effect"),
                new RewardSideEffect("Silver Side Effect"),
                new RewardSideEffect("Gold Side Effect")
        );
        Basket emptyBasket = new Basket(UUID.randomUUID(), List.of());
        Basket basket = new Basket(UUID.randomUUID(), List.of(
                new BasketItem(UUID.randomUUID(), "Hazel", 10, 2),
                new BasketItem(UUID.randomUUID(), "Hazelnut spread", 20, 4),
                new BasketItem(UUID.randomUUID(), "Potatoes", 30, 1)
        ));

        // Compute value of basket
        assertEquals(bVec(130, 0), vipPromotion.computeEarningsForBasket(basket));
        assertEquals(bVec(0, 0), vipPromotion.computeEarningsForBasket(emptyBasket));
    }

    private Vector<BigInteger> bVec(int v1, int v2) {
        return Vector.of(BigInteger.valueOf(v1), BigInteger.valueOf(v2));
    }
}