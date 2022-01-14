package org.cryptimeleon.incentive.promotion.hazel;

import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HazelRewardTest {

    HazelReward reward = new HazelReward(2, "", UUID.randomUUID(), new RewardSideEffect("Free Hazelnut Spread"));

    /*
     * Sanity check, user needs the basket points to satisfy the promotion's requirements
     */
    @Test
    void testTooExpensiveReward() {
        Vector<BigInteger> basket = bVec(1);
        Vector<BigInteger> token = bVec(0);
        assertEquals(Optional.empty(), reward.computeSatisfyingNewPointsVector(token, basket));
    }

    /*
     * Users with 1+1 points have 0 after spending 2
     */
    @Test
    void testPerfectlyAffordableReward() {
        Vector<BigInteger> basket = bVec(1);
        Vector<BigInteger> token = bVec(1);
        Vector<BigInteger> tokenAfter = bVec(0);

        assertEquals(Optional.of(tokenAfter), reward.computeSatisfyingNewPointsVector(token, basket));
        assertFalse(reward.generateRelationTree(basket).isValidForPoints(token, tokenAfter.map(b -> b.subtract(BigInteger.ONE))));
        assertTrue(reward.generateRelationTree(basket).isValidForPoints(token, tokenAfter));
        assertFalse(reward.generateRelationTree(basket).isValidForPoints(token, tokenAfter.map(b -> b.add(BigInteger.ONE))));
    }

    /*
     * Users with 1+2 points have 1 after spending 2
     */
    @Test
    void testAffordableReward() {
        Vector<BigInteger> basket = bVec(2);
        Vector<BigInteger> token = bVec(1);
        Vector<BigInteger> tokenAfter = bVec(1);

        assertEquals(Optional.of(tokenAfter), reward.computeSatisfyingNewPointsVector(token, basket));
        assertFalse(reward.generateRelationTree(basket).isValidForPoints(token, tokenAfter.map(b -> b.subtract(BigInteger.ONE))));
        assertTrue(reward.generateRelationTree(basket).isValidForPoints(token, tokenAfter));
        assertFalse(reward.generateRelationTree(basket).isValidForPoints(token, tokenAfter.map(b -> b.add(BigInteger.ONE))));
    }

    private Vector<BigInteger> bVec(int value) {
        return Vector.of(BigInteger.valueOf(value));
    }
}
