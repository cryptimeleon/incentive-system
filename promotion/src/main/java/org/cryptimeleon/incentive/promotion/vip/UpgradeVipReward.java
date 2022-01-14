package org.cryptimeleon.incentive.promotion.vip;

import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
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

public class UpgradeVipReward extends Reward {

    @Represented
    private Integer toVipStatus;

    @Represented
    private Integer accumulatedCost;

    public UpgradeVipReward(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    public UpgradeVipReward(int toVipStatus, Integer accumulatedCost, String rewardDescription, UUID rewardId) {
        super(rewardId, rewardDescription, new RewardSideEffect("Upgrade VIP status to " + toVipStatus));
        this.toVipStatus = toVipStatus;
        this.accumulatedCost = accumulatedCost;
    }

    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints) {
        assert toVipStatus > 0 && toVipStatus <= 3;
        // points stay the same and must be larger than accumulatedPoints, level must be between 1 and 1 and increased by 1 (avoid 'empty' updates)
        return new SpendDeductAndNode(
                new TokenPointsLeaf(
                        "vip-points",
                        Vector.of((BigInteger) null, null),
                        Vector.of(null, BigInteger.valueOf(toVipStatus - 1))
                ),
                new TokenUpdateLeaf(
                        "vip-update",
                        Vector.of(BigInteger.valueOf(accumulatedCost), null),
                        Vector.of((BigInteger) null, null),
                        Vector.of(BigInteger.valueOf(1), BigInteger.valueOf(0)),
                        Vector.of(basketPoints.get(0), BigInteger.valueOf(toVipStatus))
                )
        );
    }

    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        int currentStatus = tokenPoints.get(1).intValueExact();
        if (toVipStatus <= currentStatus) return Optional.empty(); // No unnecessary upgrades

        // Check if customer spent enough money for this level
        int newPoints = tokenPoints.get(0).intValueExact() + basketPoints.get(0).intValueExact();
        if (newPoints >= accumulatedCost) {
            return Optional.of(Vector.of(BigInteger.valueOf(newPoints), BigInteger.valueOf(toVipStatus)));
        }

        return Optional.empty();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}