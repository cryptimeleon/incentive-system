package org.cryptimeleon.incentive.promotion.vip;

import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VipRewardTest {

    VipReward reward = new VipReward(2, UUID.randomUUID(), new RewardSideEffect("Test Side Effect"));

    /*
     * Wrong VIP status yields no satisfying points vector.
     */
    @Test
    void testWrongVipStatusReward() {
        Vector<BigInteger> basket = bVec(0, 0);
        assertEquals(Optional.empty(), reward.computeSatisfyingNewPointsVector(bVec(0, 0), basket));
        assertEquals(Optional.empty(), reward.computeSatisfyingNewPointsVector(bVec(0, 1), basket));
        assertEquals(Optional.empty(), reward.computeSatisfyingNewPointsVector(bVec(0, 3), basket));
    }

    /*
     * Correct VIP status
     */
    @Test
    void testCorrectVipStatusReward() {
        Vector<BigInteger> token = bVec(0, 2);
        Vector<BigInteger> basket = bVec(0, 0);
        Vector<BigInteger> tokenAfter = bVec(0, 2);

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
