package org.cryptimeleon.incentive.promotion.vip;

import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UpgradeVipRewardTest {

    UpgradeVipReward reward = new UpgradeVipReward(2, 10, "This is a test upgrade reward.", UUID.randomUUID());

    /**
     * VIP status must not be the target status
     */
    @Test
    void testSameVipStatus() {
        Vector<BigInteger> token = bVec(10, 2);
        Vector<BigInteger> basket = bVec(2, 0);

        assertEquals(Optional.empty(), reward.computeSatisfyingNewPointsVector(token, basket));
    }

    /**
     * VIP status must not be larger than target status
     */
    @Test
    void testTooHighVipStatus() {
        Vector<BigInteger> token = bVec(10, 3);
        Vector<BigInteger> basket = bVec(2, 0);

        assertEquals(Optional.empty(), reward.computeSatisfyingNewPointsVector(token, basket));
    }

    /*
     * Can upgrade from VIP status None
     */
    @Test
    void testFromCorrectVipStatusNone() {
        Vector<BigInteger> token = bVec(7, 0);
        Vector<BigInteger> basket = bVec(4, 0);
        Vector<BigInteger> tokenAfter = bVec(11, 2);

        assertEquals(Optional.of(tokenAfter), reward.computeSatisfyingNewPointsVector(token, basket));
        assertTrue(reward.generateRelationTree(basket).isValidForPoints(token, tokenAfter));

        assertFalse(reward.generateRelationTree(basket).isValidForPoints(token, bVec(0, 1)));
        assertFalse(reward.generateRelationTree(basket).isValidForPoints(token, bVec(1, 2)));
        assertFalse(reward.generateRelationTree(basket).isValidForPoints(token, bVec(0, 3)));
        assertFalse(reward.generateRelationTree(basket).isValidForPoints(token, bVec(1, 2)));
    }

    private Vector<BigInteger> bVec(int v1, int v2) {
        return Vector.of(BigInteger.valueOf(v1), BigInteger.valueOf(v2));
    }
}
