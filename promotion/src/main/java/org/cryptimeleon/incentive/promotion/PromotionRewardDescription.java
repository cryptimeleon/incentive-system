package org.cryptimeleon.incentive.promotion;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents what users can gain in exchange of applying an updateOperation to their token.
 */
@AllArgsConstructor
public abstract class PromotionRewardDescription {
    @Getter
    UUID promotionRewardId;

    /**
     * Rewards might depend on the number of points given.
     * This function returns the reward
     * @param points
     * @return
     */
    abstract public Optional<PromotionReward> getPromotionRewardBasketItem(long points);
}
