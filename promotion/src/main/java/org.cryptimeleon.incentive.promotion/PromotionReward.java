package org.cryptimeleon.incentive.promotion;

import lombok.Value;

/**
 * Represents what a user can gain from spending a certain amount of points.
 */
@Value
public class PromotionReward {
    long price;
    String rewardTitle;
}
