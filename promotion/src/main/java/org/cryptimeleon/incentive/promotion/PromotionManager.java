package org.cryptimeleon.incentive.promotion;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class that holds the main promotion functionality.
 */
@Value
@AllArgsConstructor
public class PromotionManager {

    List<PromotionDescription> promotionDescriptions;

    /**
     * Computes the points that are earned for every token.
     *
     * @return map of promotionIds and increase for that promotion.
     */
    public PromotionBasketCredit computePoints(PromotionBasket promotionBasket) {
        return new PromotionBasketCredit(this.promotionDescriptions.stream()
                .collect(Collectors.toMap(PromotionDescription::getPromotionId, p -> p.computePoints(promotionBasket))));
    }

    public boolean isValidRequest(PromotionId promotionId, PromotionReward promotionReward, PromotionBasketCredit promotionBasketCredit, long tokenPoints) {
        var description = promotionDescriptions.stream().filter(d -> d.getPromotionId() == promotionId).findAny();
        if (description.isEmpty()) return false;
        return description.get().qualifiedRewards(tokenPoints + promotionBasketCredit.getCreditPerPromotion().get(promotionId)).stream().anyMatch(otherReward -> otherReward.equals(promotionReward));
    }

    public Map<PromotionId, List<PromotionReward>> computePossibleRewards(PromotionBasketCredit promotionBasketCredit, Map<PromotionId, Long> pointsPerToken) {
        return promotionDescriptions.stream().collect(Collectors.toMap(
                PromotionDescription::getPromotionId,
                d -> d.qualifiedRewards(promotionBasketCredit.getCreditPerPromotion().get(d.getPromotionId()) + pointsPerToken.get(d.getPromotionId()))
        ));
    }
}


