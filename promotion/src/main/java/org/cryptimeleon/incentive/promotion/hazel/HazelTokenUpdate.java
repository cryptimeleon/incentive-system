package org.cryptimeleon.incentive.promotion.hazel;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.EmptyTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * Earn points for basket and spend some of resulting points to get some side-effect (e.g. free items) for rewardCost
 * point.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class HazelTokenUpdate extends ZkpTokenUpdate {

    @Represented
    private Integer rewardCost;

    public HazelTokenUpdate(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    public HazelTokenUpdate(Integer rewardCost, String rewardDescription, UUID rewardId, RewardSideEffect rewardSideEffect) {
        super(rewardId, rewardDescription, rewardSideEffect);
        this.rewardCost = rewardCost;
    }

    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        // newPoints = 1 * oldPoints + basketPoints - rewardCost && newPoints >= 0
        return new TokenUpdateLeaf(
                "hazel-leaf",
                Vector.of(BigInteger.ZERO),
                Vector.of((BigInteger) null),
                Vector.of(BigInteger.ONE),
                Vector.of(basketPoints.get(0).subtract(BigInteger.valueOf(rewardCost)))
        );
    }

    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        var newPoints = tokenPoints.get(0).add(basketPoints.get(0)).subtract(BigInteger.valueOf(rewardCost));
        return newPoints.compareTo(BigInteger.ZERO) >= 0 ? Optional.of(Vector.of(newPoints)) : Optional.empty();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    /**
     * We only allow EmptyTokenUpdateMetadata since this token update and the corresponding rewards do not expect metadata.
     *
     * @param zkpTokenUpdateMetadata
     * @return whether the validation was successful or not
     */
    @Override
    public boolean validateTokenUpdateMetadata(ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        return zkpTokenUpdateMetadata instanceof EmptyTokenUpdateMetadata;
    }
}
