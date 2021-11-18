package org.cryptimeleon.incentive.promotion.promotions;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.promotion.reward.NutellaReward;
import org.cryptimeleon.incentive.promotion.reward.Reward;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.ObjectRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NutellaPromotion extends Promotion {

    public List<Reward> rewards;

    public NutellaPromotion(PromotionParameters promotionParameters, List<Reward> rewards) {
        super(promotionParameters);
        this.rewards = rewards;
    }

    public NutellaPromotion(Representation representation) {
        super(representation.obj().get("super"));
        // TODO allow other rewards using RepresentationRestorer for Rewards
        this.rewards = representation.obj().get("rewards").list().stream().map(NutellaReward::new).collect(Collectors.toList());
    }

    public static PromotionParameters generatePromotionParameters() {
        return IncentiveSystem.generatePromotionParameters(1);
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
        return this.rewards.stream()
                .filter(reward -> reward.computeSatisfyingNewPointsVector(tokenPoints, basketPoints).isPresent())
                .collect(Collectors.toList());
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation objectRepresentation = new ObjectRepresentation();
        objectRepresentation.put("super", super.getRepresentation());
        objectRepresentation.put("rewards", new ListRepresentation(rewards.stream().map(Reward::getRepresentation).collect(Collectors.toList())));
        return objectRepresentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NutellaPromotion that = (NutellaPromotion) o;
        return rewards.size() == that.rewards.size() && rewards.containsAll(that.rewards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rewards);
    }
}
