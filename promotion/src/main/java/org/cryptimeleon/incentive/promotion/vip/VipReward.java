package org.cryptimeleon.incentive.promotion.vip;

import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.Reward;
import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

// Just prove VIP status to get side effect!
public class VipReward extends Reward {

    @Represented
    private Integer requiredStatus;

    @Represented
    private Integer accumulatedCost;

    public VipReward(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    public VipReward(int requiredStatus, UUID rewardId, RewardSideEffect sideEffect) {
        super(rewardId, "Reward for VIP level " + requiredStatus, sideEffect);
        this.requiredStatus = requiredStatus;
    }

    /**
     * This VIP rewards requires the correct VIP level (second vector entry).
     * It can additionally increase the points. The following updates are enforced:
     * newPoints = 1 * oldPoints + basketPoints[0]
     * newStatus = 0 * oldStatus  + requiredStatus
     * newStatus = 1 * oldStatus + 0
     *
     * @param basketPoints a vector representing the points a user can earn for this basket
     * @return SpendDeductTree for this VIP reward
     */
    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints) {
        return new SpendDeductAndNode(
                new TokenUpdateLeaf(
                        "keep-vip-status",
                        Vector.of((BigInteger) null, null),
                        Vector.of((BigInteger) null, null),
                        Vector.of(BigInteger.ONE, BigInteger.ONE),
                        Vector.of(basketPoints.get(0), BigInteger.ZERO)
                ),
                new TokenUpdateLeaf(
                        "require-vip-status",
                        Vector.of((BigInteger) null, null),
                        Vector.of((BigInteger) null, null),
                        Vector.of(null, BigInteger.ZERO),
                        Vector.of(null, BigInteger.valueOf(requiredStatus))
                )
        );
    }

    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        if (tokenPoints.get(1).equals(BigInteger.valueOf(requiredStatus))) {
            return Optional.of(Vector.of(tokenPoints.get(0).add(basketPoints.get(0)), tokenPoints.get(1)));
        }
        return Optional.empty();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}