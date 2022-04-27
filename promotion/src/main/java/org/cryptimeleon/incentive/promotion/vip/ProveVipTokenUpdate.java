package org.cryptimeleon.incentive.promotion.vip;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.EmptyTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * Token update for proving the VIP status and increasing the counter to eventually reach the next VIP level.
 * Allows getting some side-effect for having that VIP level, e.g. some discount.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class ProveVipTokenUpdate extends ZkpTokenUpdate {

    @Represented
    private Integer requiredStatus;

    public ProveVipTokenUpdate(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    /**
     * Constructor.
     *
     * @param rewardId       every reward is identified by a unique id. This is for example useful for the user to
     *                       tell the server which update it should verify
     * @param requiredStatus the VIP level to prove
     * @param sideEffect     whatever the user gets for having that VIP level
     */
    public ProveVipTokenUpdate(UUID rewardId, int requiredStatus, RewardSideEffect sideEffect) {
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
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
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

    /**
     * Compute whether a user can afford this and how the updated token looks like.
     *
     * @param tokenPoints            the points of the token
     * @param basketPoints           the points that the basket is worth
     * @param zkpTokenUpdateMetadata metadata can provide additional input to the ZKP tree, this can be seen as public
     *                               (user) input to a ZKP
     * @return the new points vector if update possible, else an empty optional.
     */
    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        if (tokenPoints.get(1).equals(BigInteger.valueOf(requiredStatus))) {
            return Optional.of(Vector.of(tokenPoints.get(0).add(basketPoints.get(0)), tokenPoints.get(1)));
        }
        return Optional.empty();
    }

    /**
     * This promotion does not expect metadata sent by the user.
     *
     * @param zkpTokenUpdateMetadata the user metadata to verify
     * @return whether the validation was successful or not
     */
    @Override
    public boolean validateTokenUpdateMetadata(ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        return zkpTokenUpdateMetadata instanceof EmptyTokenUpdateMetadata;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
