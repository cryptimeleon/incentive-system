package org.cryptimeleon.incentive.promotion;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.sideeffect.SideEffect;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

/**
 * A token update defines a set of rules encoded in a ZKP that define how a token can be mutated. For example, such an
 * update could be the points in the new token must be 10 points less than the old token and non-negative. This could be
 * used for paying something with 10 token points.
 * Token updates can have a side-effect, e.g. if users pay for something it is the thing they get, or it could be a
 * discount on a basket, etc., but this is not always the case. Some updates might only mutate the token.
 * <p>
 * These ZKP  updates can be seen as the expressive and slow counterpart to the earn protocol. Furthermore, updates of
 * this kind will be registered in the double-spending protection database.
 */
@EqualsAndHashCode
@Getter
public abstract class ZkpTokenUpdate implements StandaloneRepresentable {

    @Represented
    private UUID tokenUpdateId;
    @Represented
    private String rewardDescription;
    @Represented
    private SideEffect sideEffect;

    /**
     * Empty constructor is needed for restoring represented objects using refletion.
     */
    protected ZkpTokenUpdate() {
    }

    /**
     * @param rewardId          every reward is identified by a unique id. This is for example useful for the user to
     *                          tell the server which update it should verify
     * @param rewardDescription a short description text on what this ZKP update actually does to display in an application on the user side
     * @param sideEffect        the side effect of this update
     */
    public ZkpTokenUpdate(UUID rewardId, String rewardDescription, RewardSideEffect sideEffect) {
        this.tokenUpdateId = rewardId;
        this.rewardDescription = rewardDescription;
        this.sideEffect = sideEffect;
    }

    /**
     * Generate the tree that represent the partial proof of knowledge that is required to get the reward.
     * The basket points vector represents what the current basket is worth, and can be conceptually added to the token points.
     * For example, if a user has 3 points on the token, the basket is worth 2 points, and the reward required 4 points
     * then the user can get 1 point with the reward, instead of having too little points.
     *
     * @param basketPoints           a vector representing the points a user can earn for this basket
     * @param zkpTokenUpdateMetadata metadata can provide additional input to the ZKP tree, this can be seen as public
     *                               (user) input to a ZKP. You might want to verify the metadata first using
     *                               {@link #validateTokenUpdateMetadata(ZkpTokenUpdateMetadata)}
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
     * @param zkpTokenUpdateMetadata metadata can provide additional input to the ZKP tree, this can be seen as public
     *                               (user) input to a ZKP
     * @return and optional vector, which returns satisfying points vector, or empty if none was found
     */
    public abstract Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata);

    public Optional<Vector<BigInteger>> computeSatisfyingNewPointsVector(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        return computeSatisfyingNewPointsVector(tokenPoints, basketPoints, new EmptyTokenUpdateMetadata());
    }

    /**
     * User-chosen metadata like timestamps needs to be verified before being used. This could be checking that it is
     * from the correct subclass, holds values in a certain range (e.g. when dealing with timestamps), etc.
     *
     * @param zkpTokenUpdateMetadata the metadata to verify
     * @return whether the validation was successful or not
     */
    public abstract boolean validateTokenUpdateMetadata(ZkpTokenUpdateMetadata zkpTokenUpdateMetadata);
}
