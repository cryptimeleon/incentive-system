package org.cryptimeleon.incentive.promotion.reward;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * A reward object identifies a reward and the conditions in the form of a ZKP relation.
 */
public abstract class Reward implements StandaloneRepresentable {

    @Represented
    private UUID rewardId;
    @Represented
    private String rewardDescription;
    @Represented
    private RewardSideEffect rewardSideEffect;

    protected Reward() {
    }

    public Reward(UUID rewardId, String rewardDescription, RewardSideEffect rewardSideEffect) {
        this.rewardId = rewardId;
        this.rewardDescription = rewardDescription;
        this.rewardSideEffect = rewardSideEffect;
    }

    /**
     * Generate the tree that represent the partial proof of knowledge that is required to get the reward.
     * The basket points vector represents what the current basket is worth, and can be offset with the token points.
     * For example, if a user has 3 points on the token, the basket is worth 2 points, and the reward required 4 points
     * then the user can get 1 point with the reward, instead of having too little points.
     *
     * @param basketPoints a vector representing the points a user can earn for this basket
     * @return a spend-deduct tree from which the ZKP that the user must provide can be generated
     */
    public abstract SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints);

    /**
     * We use partial proofs of knowledge in the underlaying crypto api, with statements that could become quite
     * powerful. Part of the witnesss required to satisfy the generated ZKP relations are the points that the new token has.
     * Determining such a vector, or even if it exists, can be non-trivial, hence we provide this function that must be
     * implemented fa reward.
     * The function returns such a points vector, or Optional.empty if none was found.
     *
     * @param tokenPoints  the points of the token
     * @param basketPoints the points that the basket is worth
     * @return and optional vector, which returns satisfying points vector, or empty if none was found
     */
    public abstract Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints);

    public UUID getRewardId() {
        return this.rewardId;
    }

    public String getRewardDescription() {
        return this.rewardDescription;
    }

    public RewardSideEffect getRewardSideEffect() {
        return this.rewardSideEffect;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Reward)) return false;
        final Reward other = (Reward) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$rewardId = this.getRewardId();
        final Object other$rewardId = other.getRewardId();
        if (this$rewardId == null ? other$rewardId != null : !this$rewardId.equals(other$rewardId))
            return false;
        final Object this$rewardDescription = this.getRewardDescription();
        final Object other$rewardDescription = other.getRewardDescription();
        if (this$rewardDescription == null ? other$rewardDescription != null : !this$rewardDescription.equals(other$rewardDescription))
            return false;
        final Object this$rewardSideEffect = this.getRewardSideEffect();
        final Object other$rewardSideEffect = other.getRewardSideEffect();
        if (this$rewardSideEffect == null ? other$rewardSideEffect != null : !this$rewardSideEffect.equals(other$rewardSideEffect))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Reward;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $rewardId = this.getRewardId();
        result = result * PRIME + ($rewardId == null ? 43 : $rewardId.hashCode());
        final Object $rewardDescription = this.getRewardDescription();
        result = result * PRIME + ($rewardDescription == null ? 43 : $rewardDescription.hashCode());
        final Object $rewardSideEffect = this.getRewardSideEffect();
        result = result * PRIME + ($rewardSideEffect == null ? 43 : $rewardSideEffect.hashCode());
        return result;
    }
}
