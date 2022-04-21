package org.cryptimeleon.incentive.promotion;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A promotion object completely determines the semantic of a promotion:
 * - The size and function of the token vector
 * - Possible updates on the token (fast-earn protocol, zkp-based-updates)
 * This class is abstract, concrete behaviour must be implemented in a subclass and its parameters.
 * Promotions are StandaloneRepresentable which allows wrapping them into a RepresentableRepresentation.
 */
@EqualsAndHashCode
@Getter
public abstract class Promotion implements StandaloneRepresentable {

    @Represented
    private PromotionParameters promotionParameters;
    @Represented
    private List<ZkpTokenUpdate> zkpTokenUpdates;
    // Name and description are for explaining the promotion to users
    @Represented
    private String promotionName;
    @Represented
    private String promotionDescription;
    @Represented
    private Boolean fastEarnSupported;

    /**
     * Empty constructor required for restoring representation of subclass.
     */
    public Promotion() {
    }

    /**
     * Constructor.
     *
     * @param promotionParameters  the promotion parameters of this promotion. This parameter should be generated with the static function of this class and it is not verified that the vector size is correct!
     * @param promotionName        name of the promotion
     * @param promotionDescription a short description of the promotion
     * @param zkpTokenUpdates      a list of all possible updates that use ZKPs
     * @param fastEarnSupported    determines whether the provider accepts earn requests based on {@link #computeEarningsForBasket(Basket)}
     */
    public Promotion(PromotionParameters promotionParameters, String promotionName, String promotionDescription, List<ZkpTokenUpdate> zkpTokenUpdates, boolean fastEarnSupported) {
        this.promotionParameters = promotionParameters;
        this.promotionName = promotionName;
        this.promotionDescription = promotionDescription;
        this.zkpTokenUpdates = zkpTokenUpdates;
        this.fastEarnSupported = fastEarnSupported;
    }

    /**
     * Hacky way of requiring a static function that generates promotion parameters for this promotion class.
     *
     * @return promotion parameters with the correct vector size for this promotion.
     */
    public static PromotionParameters generatePromotionParameters() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Computes how many points a basket is worth in terms of simple 'Earn' updates, i.e. adding a vector to to token's
     * point vector. Earn updates can be settled with a ZKP update promotion (i.e. do ZKP update and add earn amount in
     * one proof).
     * Some promotions might not utilize this feature because they require ZKPs for all updates on tokens
     * and do not need to analyze the basket for that.
     *
     * @param basket the basket to analyze
     * @return a vector of points
     */
    public abstract Vector<BigInteger> computeEarningsForBasket(Basket basket);

    /**
     * Computes which updates the points in the token combined with the earn-amount of the basket are possible.
     *
     * @param tokenPoints            points of the token
     * @param basketPoints           worth of the basket computed with {@link #computeEarningsForBasket(Basket)}
     * @param zkpTokenUpdateMetadata metadata for this promotion, e.g. a user choice or some public input to a ZKP
     * @return list of token updates
     */
    public List<ZkpTokenUpdate> computeTokenUpdatesForPoints(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints, ZkpTokenUpdateMetadata zkpTokenUpdateMetadata) {
        return this.getZkpTokenUpdates().stream()
                .filter(update -> update.computeSatisfyingNewPointsVector(tokenPoints, basketPoints, zkpTokenUpdateMetadata).isPresent())
                .collect(Collectors.toList());
    }

    /**
     * Provide default metadata for this promotion.
     * @return metadata
     */
    public ZkpTokenUpdateMetadata generateMetadataForUpdate() {
        return new EmptyTokenUpdateMetadata();
    }
}
