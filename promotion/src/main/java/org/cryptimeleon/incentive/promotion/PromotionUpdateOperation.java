package org.cryptimeleon.incentive.promotion;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PromotionUpdateOperation {
    PromotionId promotionId;
    // Either spend-deduct or credit-earn
    long tokenUpdate; // change on the token's value
    List<PromotionReward>
}
