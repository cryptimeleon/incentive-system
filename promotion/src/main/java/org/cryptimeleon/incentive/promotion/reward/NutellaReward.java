package org.cryptimeleon.incentive.promotion.reward;

import lombok.EqualsAndHashCode;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.serialization.ObjectRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * Get one free nutella for 4 points
 */
@EqualsAndHashCode
public class NutellaReward implements Reward {

    @Represented
    public Integer rewardCost;

    @Represented
    public UUID rewardId;

    public RewardSideEffect rewardSideEffect;


    public NutellaReward(int rewardCost, UUID rewardId, RewardSideEffect rewardSideEffect) {
        this.rewardCost = rewardCost;
        this.rewardId = rewardId;
        this.rewardSideEffect = rewardSideEffect;
    }

    public NutellaReward(Representation representation) {
        ObjectRepresentation objectRepresentation = (ObjectRepresentation) representation;
        ReprUtil.deserialize(this, objectRepresentation.get("reprUtil"));
        this.rewardSideEffect = new RewardSideEffect(objectRepresentation.get("rewardSideEffect"));
    }

    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints) {
        // newPoints = 1 * oldPoints + (basketPoints - 4)
        // && newPoints >= 0
        return new TokenUpdateLeaf(
                "nutella-leaf",
                Vector.of(BigInteger.ZERO),
                Vector.of((BigInteger) null),
                Vector.of(BigInteger.ONE),
                Vector.of(basketPoints.get(0).subtract(BigInteger.valueOf(4)))
        );
    }

    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        var newPoints = tokenPoints.get(0).add(basketPoints.get(0)).subtract(BigInteger.valueOf(4));
        return newPoints.compareTo(BigInteger.ZERO) >= 0 ? Optional.of(Vector.of(newPoints)) : Optional.empty();
    }

    @Override
    public RewardSideEffect getSideEffect() {
        return new RewardSideEffect("Free Nutella");
    }

    @Override
    public UUID getRewardId() {
        return this.rewardId;
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation objectRepresentation = new ObjectRepresentation();
        objectRepresentation.put("reprUtil", ReprUtil.serialize(this));
        objectRepresentation.put("rewardSideEffect", rewardSideEffect.getRepresentation());
        return objectRepresentation;
    }
}
