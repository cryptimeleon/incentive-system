package org.cryptimeleon.incentive.promotion;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class that holds the main promotion functionality.
 */
public class Promotion {

    /**
     * Computes the points that are earned for every token.
     *
     * @return map of promotionIds and increase for that promotion.
     */
    static public Map<Long, Long> computePoints(List<PromotionDescription> promotionDescriptions, Basket basket) {
        return promotionDescriptions.stream()
                .collect(Collectors.toMap(
                        promotionDescription -> promotionDescription.promotionId,
                        promotionDescription -> promotionDescription.computePoints(basket)
                ));
    }

    /**
     * Return a list of all rewards for which the given token points count qualifies one.
     */
    static public List<PromotionReward> qualifiedRewards(PromotionDescription promotionDescription, long points) {
        return promotionDescription.qualifiedRewards(points);
    }
}
