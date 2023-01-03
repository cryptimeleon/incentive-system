package org.cryptimeleon.incentive.promotion.hazel;

import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.EmptyTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.sideeffect.SideEffect;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A zkp token update that adds basket-points (points that a user could earn with the earn-protocol), then subtracts the
 * costs of some reward and ensures the resulting points count is non-zero.
 * <p>
 * This corresponds to the example from the IncentiveSystem paper, with the addition of including the points to earn to
 * avoid needing earn and spend at the same checkout.
 */
public class HazelTokenUpdate extends ZkpTokenUpdate {

    @Represented
    private Integer rewardCost;

    public HazelTokenUpdate(Representation representation) {
        super(representation);
    }

    /**
     * Constructor.
     *
     * @param rewardId          an id to uniquely identify this update instance
     * @param rewardDescription a brief description of the update
     * @param sideEffect        the side effect of this update, i.e. what the user spends the points for in this case
     * @param rewardCost        the number of points this update instance costs / that will be subtracted from the token
     */
    public HazelTokenUpdate(UUID rewardId, String rewardDescription, SideEffect sideEffect, Integer rewardCost) {
        super(rewardId, rewardDescription, sideEffect);
        this.rewardCost = rewardCost;
    }

    /**
     * Generates the relation tree for the partial proof of knowledge of this update based on worth of the basket with respect to this promotion.
     *
     * @param basketPoints           a vector representing the points a user can earn for this basket
     * @param zkpTokenUpdateMetadata metadata can provide additional input to the ZKP tree, this can be seen as public
     *                               (user) input to a ZKP. You might want to verify the metadata first using
     *                               {@link #validateTokenUpdateMetadata(ZkpTokenUpdateMetadata)}
     * @return a tree from which the crypto package can generate the ZKP instances
     */
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

    /**
     * Compute the points vector of a token after performing this update on it. Return Optional.empty() if not possible.
     *
     * @param tokenPoints            the points of the token
     * @param basketPoints           the points that the basket is worth
     * @param zkpTokenUpdateMetadata metadata can provide additional input to the ZKP tree, this can be seen as public
     *                               (user) input to a ZKP
     * @return either a new points vector (the best in the user's sense), or none if no matching vector found.
     */
    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        var newPoints = tokenPoints.get(0).add(basketPoints.get(0)).subtract(BigInteger.valueOf(rewardCost));
        return newPoints.compareTo(BigInteger.ZERO) >= 0 ? Optional.of(Vector.of(newPoints)) : Optional.empty();
    }

    /**
     * We only allow EmptyTokenUpdateMetadata since this token update and the corresponding rewards do not expect metadata.
     *
     * @param zkpTokenUpdateMetadata user metadata to validate
     * @return whether the validation was successful or not
     */
    @Override
    public boolean validateTokenUpdateMetadata(ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        return zkpTokenUpdateMetadata instanceof EmptyTokenUpdateMetadata;
    }

    public Integer getRewardCost() {
        return this.rewardCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HazelTokenUpdate that = (HazelTokenUpdate) o;
        return Objects.equals(rewardCost, that.rewardCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rewardCost);
    }
}
