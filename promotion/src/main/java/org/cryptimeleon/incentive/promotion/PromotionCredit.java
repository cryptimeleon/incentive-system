package org.cryptimeleon.incentive.promotion;

import lombok.Value;

/**
 * Data class that represents the points a user can earn from a basket for a promotion, and the points of these which
 * are already used.
 */
@Value
public class PromotionCredit {
    long creditUsed;
    long totalCredit;
}
