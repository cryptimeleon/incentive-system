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
 * Superclass for all streak updates with shared behavior. For example, the trees for the proofs of partial knowledge
 * are all similar, hence we can implement them once with some parametrization and use them in all subclasses.
 * <p>
 * Time unit is days since this is probably most natural to users, but could be extended to e.g. seconds.
 */
@Getter
abstract class StreakZkpTokenUpdate extends ZkpTokenUpdate {

    // User public input metadata timestamps may be off by this threshold
    private static final long USER_TIMESTAMP_THRESHOLD_MINUTES = 5;

    // Interval in days
    @Represented
    protected Integer intervalDays;

    public StreakZkpTokenUpdate(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    /**
     * @param rewardId          every reward is identified by a unique id. This is for example useful for the user to
     *                          tell the server which update it should verify
     * @param rewardDescription a short description text on what this ZKP update actually does to display in an application on the user side
     * @param rewardSideEffect  the side effect of this update
     * @param intervalDays      the interval in which the streak needs to be updates to not get lost.
     */
    public StreakZkpTokenUpdate(UUID rewardId, String rewardDescription, RewardSideEffect rewardSideEffect, int intervalDays) {
        super(rewardId, rewardDescription, rewardSideEffect);
        this.intervalDays = intervalDays;
    }

    /**
     * ZKP statement for incrementing a streak within the time constraints without additional checks or requirements.
     *
     * @param now the current timestamp as long
     * @return tree representing the statement to prove with zero knowledge
     */
    protected SpendDeductTree updateTree(long now) {
        return updateTree(now, null, null);
    }

    /**
     * ZKP statement for incrementing a streak within the time constraints with additional range proof on the streak
     *
     * @param now                    the current timestamp as long
     * @param streakGreaterOrEqualTo lower bound for range proof. Only allows streaks that have at least this value
     * @return tree representing the statement to prove with zero knowledge
     */
    protected SpendDeductTree updateTreeRangeProof(long now, int streakGreaterOrEqualTo) {
        return updateTree(now, streakGreaterOrEqualTo, null);
    }

    /**
     * ZKP statement for incrementing a streak within the time constraints with additionally subtracting some of the streak points.
     *
     * @param now                 the current timestamp as long
     * @param streakPointsToSpend the number of points a user needs to subtract from the streak e.g. for a promotion
     * @return tree representing the statement to prove with zero knowledge
     */
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


    /**
     * Generates a zkp tree for the statement that a User needs to restart streak and starts from zero!
     *
     * @param now current timestamp
     * @return tree for this statement
     */
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

    /**
     * Utility function that perform a type cast on some ZkpTokenUpdateMetadata and returns a matching timestamp instance if possible.
     *
     * @param zkpTokenUpdateMetadata the metadata to cast
     * @return the metadata casted to the timestamp type
     */
    protected StreakTokenUpdateTimestamp toTimestampMetadata(ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        if (!(zkpTokenUpdateMetadata instanceof StreakTokenUpdateTimestamp)) {
            throw new RuntimeException("Expected Metadata of type StreakTokenUpdateTimestamp");
        }

        return ((StreakTokenUpdateTimestamp) zkpTokenUpdateMetadata);
    }

    /**
     * Returns a timestamp fro the current day
     *
     * @return timestamp as long
     */
    private long epochDays() {
        return epochDaysOf(LocalDate.now());
    }

    /**
     * Returns the epoch timestamp in days of some datetime
     *
     * @param timestamp the datetime timestamp to convert
     * @return epoch day timestamp
     */
    private long epochDaysOf(LocalDate timestamp) {
        LocalDate epoch = LocalDate.ofEpochDay(0);
        return ChronoUnit.DAYS.between(epoch, timestamp);
    }

    /**
     * Timestamps used by users to generate their proofs are valid for {@link #USER_TIMESTAMP_THRESHOLD_MINUTES} minutes.
     * This avoids problems with updates around midnight.
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
        // Check whether the day is today, was yesterday and yesterday is at most some threshold away, or the same for tomorrow
        return epochDays() == userEpochDay
                || epochDaysOf(LocalDateTime.now().minusMinutes(USER_TIMESTAMP_THRESHOLD_MINUTES).toLocalDate()) == userEpochDay
                || epochDaysOf(LocalDateTime.now().plusMinutes(USER_TIMESTAMP_THRESHOLD_MINUTES).toLocalDate()) == userEpochDay;

    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
