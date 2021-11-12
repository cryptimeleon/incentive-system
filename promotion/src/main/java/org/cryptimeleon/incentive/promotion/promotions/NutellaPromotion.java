package org.cryptimeleon.incentive.promotion.promotions;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.promotion.reward.NutellaReward;
import org.cryptimeleon.incentive.promotion.reward.Reward;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;

public class NutellaPromotion extends Promotion {

    public NutellaPromotion(PromotionParameters promotionParameters) {
        super(promotionParameters);
    }

    public NutellaPromotion(IncentiveSystem incentiveSystem) {
        super(incentiveSystem.generatePromotionParameters(1));
    }

    @Override
    public Vector<BigInteger> computeEarningsForBasket(Basket basket) {
        return Vector.of(BigInteger.valueOf(
                basket.basketItemList.stream()
                        .filter(basketItem -> basketItem.getTitle().toLowerCase().contains("nutella"))
                        .map(BasketItem::getCount)
                        .reduce(Integer::sum)
                        .orElseThrow()
        ));
    }

    @Override
    public List<Reward> computeRewardsForPoints(Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        return List.of(new NutellaReward());
    }
}
