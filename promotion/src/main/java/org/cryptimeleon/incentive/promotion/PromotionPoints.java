package org.cryptimeleon.incentive.promotion;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PromotionPoints {
    long promotionId;
    long points;
}
