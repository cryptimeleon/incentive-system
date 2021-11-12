package org.cryptimeleon.incentive.promotion.model;

import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.incentive.promotion.reward.Reward;

/*
 * This choice might be extended to be more powerful:
 * In some cases, the basket knowledge can be sufficient to satisfy the rewards conditions, and hence no Spend run be required.
 * In these cases, the SpendRequest can be omitted, or replaced by an EarnRequest for remaining points.
 */
public class RewardChoice {
    public final SpendRequest spendRequest;
    public final Reward reward;

    public RewardChoice(SpendRequest spendRequest, Reward reward) {
        this.spendRequest = spendRequest;
        this.reward = reward;
    }
}
