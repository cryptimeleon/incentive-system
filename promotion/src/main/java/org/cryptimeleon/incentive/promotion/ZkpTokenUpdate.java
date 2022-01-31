package org.cryptimeleon.incentive.promotion;

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
public abstract class ZkpTokenUpdate implements StandaloneRepresentable {

    @Represented
    private UUID tokenUpdateId;
    @Represented
    private String rewardDescription;
    @Represented
    private RewardSideEffect rewardSideEffect;

    protected ZkpTokenUpdate() {
    }

    public ZkpTokenUpdate(UUID rewardId, String rewardDescription, RewardSideEffect rewardSideEffect) {
        this.tokenUpdateId = rewardId;
        this.rewardDescription = rewardDescription;
        this.rewardSideEffect = rewardSideEffect;
    }

    /**
     * Generate the tree that represent the partial proof of knowledge that is required to get the reward.
     * The basket points vector represents what the current basket is worth, and can be offset with the token points.
     * For example, if a user has 3 points on the token, the basket is worth 2 points, and the reward required 4 points
     * then the user can get 1 point with the reward, instead of having too little points.
     *
     * @param basketPoints           a vector representing the points a user can earn for this basket
     * @param zkpTokenUpdateMetadata
     * @return a spend-deduct tree from which the ZKP that the user must provide can be generated
     */
    public abstract SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata);

    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints) {
        return generateRelationTree(basketPoints, new EmptyTokenUpdateMetadata());
    }


    /**
     * We use partial proofs of knowledge in the underlying crypto api, with statements that could become quite
     * powerful. Part of the witness required to satisfy the generated ZKP relations are the points that the new token has.
     * Determining such a vector, or even if it exists, can be non-trivial, hence we provide this function that must be
     * implemented fa reward.
     * The function returns such a points vector, or Optional.empty if none was found.
     *
     * @param tokenPoints            the points of the token
     * @param basketPoints           the points that the basket is worth
     * @param zkpTokenUpdateMetadata
     * @return and optional vector, which returns satisfying points vector, or empty if none was found
     */
    public abstract Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata);

    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        return computeSatisfyingNewPointsVector(tokenPoints, basketPoints, new EmptyTokenUpdateMetadata());
    }

    /**
     * User-chosen metadata like timestamps needs to be verified before being used.
     *
     * @param zkpTokenUpdateMetadata
     * @return whether the validation was successful or not
     */
    public abstract boolean validateTokenUpdateMetadata(ZkpTokenUpdateMetadata zkpTokenUpdateMetadata);


    public UUID getTokenUpdateId() {
        return this.tokenUpdateId;
    }

    public String getRewardDescription() {
        return this.rewardDescription;
    }

    public RewardSideEffect getRewardSideEffect() {
        return this.rewardSideEffect;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ZkpTokenUpdate)) return false;
        final ZkpTokenUpdate other = (ZkpTokenUpdate) o;
        if (!other.canEqual(this)) return false;
        final Object this$rewardId = this.getTokenUpdateId();
        final Object other$rewardId = other.getTokenUpdateId();
        if (this$rewardId == null ? other$rewardId != null : !this$rewardId.equals(other$rewardId))
            return false;
        final Object this$rewardDescription = this.getRewardDescription();
        final Object other$rewardDescription = other.getRewardDescription();
        if (this$rewardDescription == null ? other$rewardDescription != null : !this$rewardDescription.equals(other$rewardDescription))
            return false;
        final Object this$rewardSideEffect = this.getRewardSideEffect();
        final Object other$rewardSideEffect = other.getRewardSideEffect();
        return this$rewardSideEffect == null ? other$rewardSideEffect == null : this$rewardSideEffect.equals(other$rewardSideEffect);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ZkpTokenUpdate;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $rewardId = this.getTokenUpdateId();
        result = result * PRIME + ($rewardId == null ? 43 : $rewardId.hashCode());
        final Object $rewardDescription = this.getRewardDescription();
        result = result * PRIME + ($rewardDescription == null ? 43 : $rewardDescription.hashCode());
        final Object $rewardSideEffect = this.getRewardSideEffect();
        result = result * PRIME + ($rewardSideEffect == null ? 43 : $rewardSideEffect.hashCode());
        return result;
    }
}
