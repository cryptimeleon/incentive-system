package org.cryptimeleon.incentive.promotion.streak;

import lombok.Getter;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Superclass for Streak update implementations with shared behaviour.
 * Time unit is days since this is probably most natural to users, but could be extended to e.g. seconds.
 */
@Getter
abstract class StreakZkpTokenUpdate extends ZkpTokenUpdate {

    // Interval in days
    @Represented
    protected Integer intervalDays;

    public StreakZkpTokenUpdate(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    public StreakZkpTokenUpdate(UUID rewardId, String rewardDescription, RewardSideEffect rewardSideEffect, int intervalDays) {
        super(rewardId, rewardDescription, rewardSideEffect);
        this.intervalDays = intervalDays;
    }

    // Case in which user can update streak
    protected SpendDeductTree updateTree(long now) {
        return updateTree(now, null, null);
    }

    protected SpendDeductTree updateTreeRangeProof(long now, int streakGreaterOrEqualTo) {
        return updateTree(now, streakGreaterOrEqualTo, null);
    }

    protected SpendDeductTree updateTreeAndSpend(long now, int streakPointsToSpend) {
        return updateTree(now, 0, streakPointsToSpend);
    }

    /**
     * Case in which user can update streak and new value is larger than some lower bound
     *
     * @param now                    current epoch timestamp in millis
     * @param streakGreaterOrEqualTo lower bound for streak after update to satisfy. use null to ignore
     * @param subtractFromStreak     subtract this many points from the updated streak score. null to ignore
     * @return the spend-deduct tree for this update
     */
    private SpendDeductTree updateTree(long now, Integer streakGreaterOrEqualTo, Integer subtractFromStreak) {
        return new SpendDeductAndNode(
                new TokenPointsLeaf("previous-in-range",
                        Vector.of(null, BigInteger.valueOf(now - intervalDays)),
                        Vector.of((BigInteger) null, null)
                ),
                new TokenUpdateLeaf("update-in-range",
                        Vector.of(streakGreaterOrEqualTo != null ? BigInteger.valueOf(streakGreaterOrEqualTo) : null, null),
                        Vector.of((BigInteger) null, null),
                        Vector.of(BigInteger.ONE, BigInteger.ZERO),
                        Vector.of(subtractFromStreak != null ? BigInteger.valueOf(1 - subtractFromStreak) : BigInteger.ONE, BigInteger.valueOf(now))
                )
        );
    }

    // User needs to restart streak: Start from zero!
    protected SpendDeductTree resetTree(long now) {
        return new SpendDeductAndNode(
                // This PointsLeaf might be removed in the future, right now it only ensures that users only have a witness for either one of the Or children.
                new TokenPointsLeaf("previous-not-in-range",
                        Vector.of((BigInteger) null, null),
                        Vector.of(null, BigInteger.valueOf(now - intervalDays))
                ),
                new TokenUpdateLeaf("start-from-zero",
                        Vector.of((BigInteger) null, null),
                        Vector.of((BigInteger) null, null),
                        Vector.of(BigInteger.ZERO, BigInteger.ZERO),
                        Vector.of(BigInteger.ONE, BigInteger.valueOf(now))
                )
        );
    }

    protected StreakTokenUpdateTimestamp toTimestampMetadata(ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        if (!(zkpTokenUpdateMetadata instanceof StreakTokenUpdateTimestamp)) {
            throw new RuntimeException("Expected Metadata of type StreakTokenUpdateTimestamp");
        }

        return ((StreakTokenUpdateTimestamp) zkpTokenUpdateMetadata);
    }

    public Long epochDays() {
        return epochDaysOf(LocalDate.now());
    }

    private Long epochDaysOf(LocalDate timestamp) {
        LocalDate epoch = LocalDate.ofEpochDay(0);
        return ChronoUnit.DAYS.between(epoch, timestamp);
    }

    /**
     * Timestamps used by users to generate their proofs are valid for 5 minutes.
     *
     * @param zkpTokenUpdateMetadata the user timestamp as metadata
     * @return true if the timestamp is at most five minutes old
     */
    @Override
    public boolean validateTokenUpdateMetadata(ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        if (!(zkpTokenUpdateMetadata instanceof StreakTokenUpdateTimestamp)) {
            throw new RuntimeException("Expected Metadata of type StreakTokenUpdateTimestamp");
        }
        long userEpochDay = ((StreakTokenUpdateTimestamp) zkpTokenUpdateMetadata).getTimestamp();
        return epochDays() == userEpochDay
                || epochDaysOf(LocalDateTime.now().minusMinutes(5).toLocalDate()) == userEpochDay
                || epochDaysOf(LocalDateTime.now().plusMinutes(5).toLocalDate()) == userEpochDay;

    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
