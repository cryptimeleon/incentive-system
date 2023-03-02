package org.cryptimeleon.incentive.promotion.hazel;

import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HazelPromotionTest {

    HazelPromotion hazelPromotion = new HazelPromotion(HazelPromotion.generatePromotionParameters(),
            "Test Promotion",
            "This is another Test Promotion",
            List.of(),
            "hAzel"); // Make sure case is ignored

    /**
     * Test counting items that match a selector.
     */
    @Test
    void testComputeEarningsForBasket() {
        Basket basket = new Basket(UUID.randomUUID(), List.of(
                new BasketItem(UUID.randomUUID().toString(), "Hazel", 400, 2),
                new BasketItem(UUID.randomUUID().toString(), "Hazelnut spread", 400, 4),
                new BasketItem(UUID.randomUUID().toString(), "Potatoes", 50, 1)
        ));

        // Compute value of basket
        Vector<BigInteger> basketPoints = hazelPromotion.computeEarningsForBasket(basket);
        assertEquals(Vector.of(BigInteger.valueOf(6)), basketPoints);
    }

    @Test
    void allowsEarnProtocol() {
        assertTrue(hazelPromotion.getFastEarnSupported());
    }
}
