package org.cryptimeleon.incentive.promotion.streak;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StreakRewardTest {

    StreakTimeUtil streakTimeUtil = mock(StreakTimeUtil.class);

    /**
     * Test correct evaluation if the new date is within the time for a streak
     */
    @Test
    void testValidDate() {
        when(streakTimeUtil.getTodayAsEpochDay()).thenReturn(14L); // Today is the 14th

        StreakReward streakReward = new StreakReward(UUID.randomUUID(),
                "Some Reward",
                new RewardSideEffect("Some Side Effect"),
                7,
                streakTimeUtil);
        SpendDeductTree relationTree = streakReward.generateRelationTree(Vector.of(BigInteger.ZERO, BigInteger.ZERO));

        // Test valid range yields valid
        Assertions.assertTrue(relationTree.isValidForPoints(
                Vector.of(BigInteger.valueOf(2), BigInteger.valueOf(7)),
                Vector.of(BigInteger.valueOf(3), BigInteger.valueOf(14))
        ));

        // Check that users can only increase their streak by 1
        Assertions.assertFalse(relationTree.isValidForPoints(
                Vector.of(BigInteger.valueOf(2), BigInteger.valueOf(7)),
                Vector.of(BigInteger.valueOf(4), BigInteger.valueOf(14))
        ));

        // Test new time is verified correctly
        Assertions.assertFalse(relationTree.isValidForPoints(
                Vector.of(BigInteger.valueOf(1), BigInteger.valueOf(7)),
                Vector.of(BigInteger.valueOf(2), BigInteger.valueOf(15))
        ));
    }

    /**
     * Test a correct points vector is computed for a valid streak update.
     */
    @Test
    void testValidDatePointsVector() {
        when(streakTimeUtil.getTodayAsEpochDay()).thenReturn(14L); // Today is the 14th

        StreakReward streakReward = new StreakReward(UUID.randomUUID(),
                "Some Reward",
                new RewardSideEffect("Some Side Effect"),
                7,
                streakTimeUtil);

        // Check correct point vectors are computed
        Optional<Vector<BigInteger>> satisfyingPointsVector = streakReward.computeSatisfyingNewPointsVector(
                Vector.of(BigInteger.valueOf(2), BigInteger.valueOf(7)),
                Vector.of(BigInteger.ZERO, BigInteger.ZERO)
        );
        Assertions.assertTrue(satisfyingPointsVector.isPresent());
        Assertions.assertEquals(
                Vector.of(BigInteger.valueOf(3), BigInteger.valueOf(14)),
                satisfyingPointsVector.get()
        );
    }

    /**
     * Test that users cannot update their streak if the last time shopping was to too long ago
     */
    @Test
    void testInvalidDateUpdate() {
        when(streakTimeUtil.getTodayAsEpochDay()).thenReturn(15L); // Today is the 15th

        StreakReward streakReward = new StreakReward(UUID.randomUUID(),
                "Some Reward",
                new RewardSideEffect("Some Side Effect"),
                7,
                streakTimeUtil);
        SpendDeductTree relationTree = streakReward.generateRelationTree(Vector.of(BigInteger.ZERO, BigInteger.ZERO));

        Assertions.assertFalse(relationTree.isValidForPoints(
                Vector.of(BigInteger.valueOf(1), BigInteger.valueOf(7)),
                Vector.of(BigInteger.valueOf(2), BigInteger.valueOf(15))
        ));
    }

    /**
     * Test that users cannot update their streak if the last time shopping was to too long ago
     */
    @Test
    void testInvalidDateReset() {
        when(streakTimeUtil.getTodayAsEpochDay()).thenReturn(15L); // Today is the 15th

        StreakReward streakReward = new StreakReward(UUID.randomUUID(),
                "Some Reward",
                new RewardSideEffect("Some Side Effect"),
                7,
                streakTimeUtil);
        SpendDeductTree relationTree = streakReward.generateRelationTree(Vector.of(BigInteger.ZERO, BigInteger.ZERO));

        Assertions.assertTrue(relationTree.isValidForPoints(
                Vector.of(BigInteger.valueOf(1), BigInteger.valueOf(7)),
                Vector.of(BigInteger.valueOf(1), BigInteger.valueOf(15))
        ));
    }

    /**
     * Test that for a streak that will be reset a valid points vector in computed
     */
    @Test
    void testInvalidDatePointsVector() {
        when(streakTimeUtil.getTodayAsEpochDay()).thenReturn(15L); // Today is the 15th

        StreakReward streakReward = new StreakReward(UUID.randomUUID(),
                "Some Reward",
                new RewardSideEffect("Some Side Effect"),
                7,
                streakTimeUtil);

        // Check correct point vectors are computed, streak reset to 1 and date set to 15
        Optional<Vector<BigInteger>> satisfyingPointsVector = streakReward.computeSatisfyingNewPointsVector(
                Vector.of(BigInteger.valueOf(2), BigInteger.valueOf(7)),
                Vector.of(BigInteger.ZERO, BigInteger.ZERO)
        );
        Assertions.assertTrue(satisfyingPointsVector.isPresent());
        Assertions.assertEquals(
                Vector.of(BigInteger.valueOf(1), BigInteger.valueOf(15)),
                satisfyingPointsVector.get()
        );
    }
}