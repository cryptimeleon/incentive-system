package org.cryptimeleon.incentive.promotion.vip;

import lombok.EqualsAndHashCode;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.Reward;
import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
public class VipPromotion extends Promotion {

    public static final int GOLD = 3;
    public static final int SILVER = 2;
    public static final int BRONZE = 1;
    public static final int NONE = 0;

    public VipPromotion(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

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
                        goldSideEffect)
        );
    }

    private static List<Reward> computeRewards(
            int costBronze,
            int costSilver,
            int costGold,
            RewardSideEffect bronzeSideEffect,
            RewardSideEffect silverSideEffect,
            RewardSideEffect goldSideEffect) {

        assert costBronze < costSilver && costSilver < costGold;

        return List.of(
                new UpgradeVipReward(BRONZE, costBronze, "Get Bronze VIP Status", UUID.randomUUID()),
                new UpgradeVipReward(SILVER, costSilver, "Get Silver VIP Status", UUID.randomUUID()),
                new UpgradeVipReward(GOLD, costGold, "Get Gold VIP Status", UUID.randomUUID()),
                new VipReward(BRONZE, UUID.randomUUID(), bronzeSideEffect),
                new VipReward(SILVER, UUID.randomUUID(), silverSideEffect),
                new VipReward(GOLD, UUID.randomUUID(), goldSideEffect)
        );
    }

    public static PromotionParameters generatePromotionParameters() {
        return IncentiveSystem.generatePromotionParameters(2);
    }

    @Override
    public Vector<BigInteger> computeEarningsForBasket(Basket basket) {
        return Vector.of(BigInteger.valueOf(basket.computeBasketValue()), BigInteger.ZERO);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
