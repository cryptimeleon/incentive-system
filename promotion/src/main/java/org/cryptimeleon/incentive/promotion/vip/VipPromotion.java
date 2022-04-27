package org.cryptimeleon.incentive.promotion.vip;

import lombok.EqualsAndHashCode;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

/**
 * Promotion in which users can earn a VIP status (BRONZE/SILVER/GOLD) for spent money and prove their current status.
 * The updates are modelled such that it is revealed when a user upgrades to the next status.
 * This could be avoided by using a more expensive OR proof, but we think revealing such an update is not a problem with
 * our traceability guarantees.
 * <p>
 * This promotion does support the fast earn protocol, but users cannot prove their VIP level when doing this (works
 * well for level NONE).
 */
@EqualsAndHashCode(callSuper = true)
public class VipPromotion extends Promotion {

    public static final int GOLD = 3;
    public static final int SILVER = 2;
    public static final int BRONZE = 1;
    public static final int NONE = 0;

    public VipPromotion(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    /**
     * Constructor .
     *
     * @param promotionParameters  the promotion parameters of this promotion. This parameter should be generated with the static function of this class and it is not verified that the vector size is correct!
     * @param promotionName        name of the promotion
     * @param promotionDescription a short description of the promotion
     * @param costBronze           the number of points to reach bronze
     * @param costSilver           the number of points to reach silver
     * @param costGold             the number of points to reach gold
     * @param bronzeSideEffect     the side effect when proving bronze VIP level
     * @param goldSideEffect       the side effect when proving silver VIP level
     * @param silverSideEffect     the side effect when proving gold VIP level
     */
    public VipPromotion(PromotionParameters promotionParameters,
                        String promotionName,
                        String promotionDescription,
                        int costBronze,
                        int costSilver,
                        int costGold,
                        RewardSideEffect bronzeSideEffect,
                        RewardSideEffect silverSideEffect,
                        RewardSideEffect goldSideEffect) {
        super(promotionParameters,
                promotionName,
                promotionDescription,
                computeRewards(costBronze,
                        costSilver,
                        costGold,
                        bronzeSideEffect,
                        silverSideEffect,
                        goldSideEffect),
                true// No fast earn, all updates must be through ZKPs
        );
    }

    /**
     * A utility method used for bootstrapping all updates of a VIP promotion.
     *
     * @param costBronze       the number of points to reach bronze
     * @param costSilver       the number of points to reach silver
     * @param costGold         the number of points to reach gold
     * @param bronzeSideEffect the side effect when proving bronze VIP level
     * @param goldSideEffect   the side effect when proving silver VIP level
     * @param silverSideEffect the side effect when proving gold VIP level
     * @return a list of token updates for the VIP promotion
     */
    private static List<ZkpTokenUpdate> computeRewards(
            int costBronze,
            int costSilver,
            int costGold,
            RewardSideEffect bronzeSideEffect,
            RewardSideEffect silverSideEffect,
            RewardSideEffect goldSideEffect) {

        // Sanity check for costs
        assert costBronze < costSilver && costSilver < costGold;

        // Update ZKPs for every target level and updates for maintaining a VIP level and proving being VIP.
        // For NONE or if no side-effect desired use earn protocol
        return List.of(
                new UpgradeVipZkpTokenUpdate(UUID.randomUUID(), "Get Bronze VIP Status", BRONZE, costBronze),
                new UpgradeVipZkpTokenUpdate(UUID.randomUUID(), "Get Silver VIP Status", SILVER, costSilver),
                new UpgradeVipZkpTokenUpdate(UUID.randomUUID(), "Get Gold VIP Status", GOLD, costGold),
                new ProveVipTokenUpdate(UUID.randomUUID(), BRONZE, bronzeSideEffect),
                new ProveVipTokenUpdate(UUID.randomUUID(), SILVER, silverSideEffect),
                new ProveVipTokenUpdate(UUID.randomUUID(), GOLD, goldSideEffect)
        );
    }

    public static PromotionParameters generatePromotionParameters() {
        return IncentiveSystem.generatePromotionParameters(2);
    }

    /**
     * Compute how much the basket is worth. In this case we just take the basket value.
     *
     * @param basket the basket to analyze
     * @return basket worth in terms of token vector updates
     */
    @Override
    public Vector<BigInteger> computeEarningsForBasket(Basket basket) {
        return Vector.of(BigInteger.valueOf(basket.computeBasketValue()), BigInteger.ZERO);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
