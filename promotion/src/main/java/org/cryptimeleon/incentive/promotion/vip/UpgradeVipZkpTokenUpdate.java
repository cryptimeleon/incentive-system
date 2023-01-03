package org.cryptimeleon.incentive.promotion.vip;

import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
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
 * Token update for the VIP promotion in which the VIP status is upgraded to the next level.
 */
public class UpgradeVipZkpTokenUpdate extends ZkpTokenUpdate {

    @Represented
    private Integer toVipStatus;

    @Represented
    private Integer accumulatedCost;

    public UpgradeVipZkpTokenUpdate(Representation representation) {
        super(representation);
    }

    /**
     * Constructor.
     *
     * @param rewardId          every reward is identified by a unique id. This is for example useful for the user to
     *                          tell the server which update it should verify
     * @param rewardDescription a short description text on what this ZKP update actually does to display in an application on the user side
     * @param toVipStatus       the target status as an integer (1-bronze, 2-silver, 3-gold)
     * @param accumulatedCost   the cost of reaching this VIP status (accumulated with the previous costs)
     */
    public UpgradeVipZkpTokenUpdate(UUID rewardId, String rewardDescription, int toVipStatus, Integer accumulatedCost, SideEffect sideEffect) {
        super(rewardId, rewardDescription, sideEffect);
        this.toVipStatus = toVipStatus;
        this.accumulatedCost = accumulatedCost;
    }

    /**
     * Generate a ZKP tree for this token update.
     *
     * @param basketPoints           a vector representing the points a user can earn for this basket
     * @param zkpTokenUpdateMetadata metadata can provide additional input to the ZKP tree, this can be seen as public
     *                               (user) input to a ZKP. You might want to verify the metadata first using
     *                               {@link #validateTokenUpdateMetadata(ZkpTokenUpdateMetadata)}
     * @return a tree for generating the ZKP.
     */
    @Override
    public SpendDeductTree generateRelationTree(Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
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

    /**
     * Compute whether one can afford this update and how the updated token would look like.
     *
     * @param tokenPoints            the points of the token
     * @param basketPoints           the points that the basket is worth
     * @param zkpTokenUpdateMetadata metadata can provide additional input to the ZKP tree, this can be seen as public
     *                               (user) input to a ZKP
     * @return Value of updated token if update possible, else empty optional.
     */
    @Override
    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        int currentStatus = tokenPoints.get(1).intValueExact();
        if (toVipStatus <= currentStatus) return Optional.empty(); // No unnecessary upgrades

        // Check if customer spent enough money for this level
        int newPoints = tokenPoints.get(0).intValueExact() + basketPoints.get(0).intValueExact();
        if (newPoints >= accumulatedCost) {
            return Optional.of(Vector.of(BigInteger.valueOf(newPoints), BigInteger.valueOf(toVipStatus)));
        }

        return Optional.empty();
    }


    /**
     * This promotion does not expect any metadata.
     *
     * @param zkpTokenUpdateMetadata metadata sent by the user along the request
     * @return whether the validation was successful or not
     */
    @Override
    public boolean validateTokenUpdateMetadata(ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        return zkpTokenUpdateMetadata instanceof EmptyTokenUpdateMetadata;
    }

    public Integer getToVipStatus() {
        return this.toVipStatus;
    }

    public Integer getAccumulatedCost() {
        return this.accumulatedCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UpgradeVipZkpTokenUpdate that = (UpgradeVipZkpTokenUpdate) o;
        return Objects.equals(toVipStatus, that.toVipStatus) && Objects.equals(accumulatedCost, that.accumulatedCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), toVipStatus, accumulatedCost);
    }
}
