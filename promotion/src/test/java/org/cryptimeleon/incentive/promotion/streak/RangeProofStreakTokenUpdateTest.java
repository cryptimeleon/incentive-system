package org.cryptimeleon.incentive.promotion.streak;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

public class RangeProofStreakTokenUpdateTest {

    RangeProofStreakTokenUpdate standardStreakTokenUpdate = new RangeProofStreakTokenUpdate(UUID.randomUUID(),
            "Some Reward",
            new RewardSideEffect("Some Side Effect"),
            7,
            4);

    /**
     * Ensure user time input is required.
     */
    @Test
    void testMetadataTypeValidation() {
        Assertions.assertThrows(RuntimeException.class, () -> standardStreakTokenUpdate.generateRelationTree(Vector.of(BigInteger.ZERO, BigInteger.ZERO)));
    }

    /**
     * Test correct evaluation if the new date is within the time for a streak and the streak is large enough
     */
    @Test
    void testValidDate() {
        SpendDeductTree relationTree = standardStreakTokenUpdate.generateRelationTree(Vector.of(BigInteger.ZERO, BigInteger.ZERO), new StreakTokenUpdateTimestamp(14L));

        // Test valid range yields valid
        Assertions.assertTrue(relationTree.isValidForPoints(
                Vector.of(BigInteger.valueOf(4), BigInteger.valueOf(7L)),
                Vector.of(BigInteger.valueOf(5), BigInteger.valueOf(14L))
        ));
        Assertions.assertTrue(relationTree.isValidForPoints(
                Vector.of(BigInteger.valueOf(5), BigInteger.valueOf(7L)),
                Vector.of(BigInteger.valueOf(6), BigInteger.valueOf(14L))
        ));

        // Test update into the valid range yields valid
        Assertions.assertTrue(relationTree.isValidForPoints(
                Vector.of(BigInteger.valueOf(3), BigInteger.valueOf(7L)),
                Vector.of(BigInteger.valueOf(4), BigInteger.valueOf(14L))
        ));

        // Check that users can only increase their streak by 1
        Assertions.assertFalse(relationTree.isValidForPoints(
                Vector.of(BigInteger.valueOf(2), BigInteger.valueOf(7L)),
                Vector.of(BigInteger.valueOf(3), BigInteger.valueOf(14L))
        ));

        // Test new time is verified correctly
        Assertions.assertFalse(relationTree.isValidForPoints(
                Vector.of(BigInteger.valueOf(3), BigInteger.valueOf(7L)),
                Vector.of(BigInteger.valueOf(4), BigInteger.valueOf(15L))
        ));
    }

    /**
     * Test a correct points vector is computed for a valid streak update.
     */
    @Test
    void testValidDatePointsVector() {
        // Check correct point vectors are computed
        Optional<Vector<BigInteger>> satisfyingPointsVector = standardStreakTokenUpdate.computeSatisfyingNewPointsVector(
                Vector.of(BigInteger.valueOf(3), BigInteger.valueOf(7)),
                Vector.of(BigInteger.ZERO, BigInteger.ZERO),
                new StreakTokenUpdateTimestamp(14L)
        );
        Assertions.assertTrue(satisfyingPointsVector.isPresent());
        Assertions.assertEquals(
                Vector.of(BigInteger.valueOf(4), BigInteger.valueOf(14)),
                satisfyingPointsVector.get()
        );
    }

    /**
     * Test that users cannot update their streak if the last time shopping was to too long ago
     */
    @Test
    void testInvalidDateUpdate() {
        SpendDeductTree relationTree = standardStreakTokenUpdate.generateRelationTree(Vector.of(BigInteger.ZERO, BigInteger.ZERO),
                new StreakTokenUpdateTimestamp(15L)
        );

        Assertions.assertFalse(relationTree.isValidForPoints(
                Vector.of(BigInteger.valueOf(3), BigInteger.valueOf(7)),
                Vector.of(BigInteger.valueOf(4), BigInteger.valueOf(15))
        ));
    }
}
