package org.cryptimeleon.incentive.promotion;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.SpendProviderOutput;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.EarnPointsChoice;
import org.cryptimeleon.incentive.promotion.model.RewardChoice;
import org.cryptimeleon.incentive.promotion.promotions.Promotion;
import org.cryptimeleon.incentive.promotion.reward.Reward;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;

// TODO remove this wrapper, not really that useful
@Deprecated
public class PromotionManager {

    final IncentiveSystem incentiveSystem;
    final IncentivePublicParameters pp;

    public PromotionManager(IncentiveSystem incentiveSystem) {
        this.incentiveSystem = incentiveSystem;
        this.pp = incentiveSystem.pp;
    }

    // Common Api
    SpendDeductTree generateSpendDeductTree(Promotion promotion, Reward reward, Vector<BigInteger> basketPoints) {
        // TODO reward must be somewhat trusted
        return reward.generateRelationTree(basketPoints);
    }

    /**
     * Function that depends on the promotion and basket only.
     * Determines the points that a user earn for it.
     * This is completely independent from the user's token.
     * Must be deterministic since it should be subject of the credit-earn protocol
     */
    Vector<BigInteger> computeEarningsForBasket(Promotion promotion, Basket basket) {
        return promotion.computeEarningsForBasket(basket);
    }


    // User centered api

    /**
     * Function for determining the rewards that a user could redeem based on it's points vector.
     * These can include the points a user will earn in this transaction (e.g. hazelnut spread example),
     * or exclude them (you earn 5 points for every euro spent
     */
    List<Reward> computeRewardsForPoints(Promotion promotion, Vector<BigInteger> tokenPoints, Vector<BigInteger> basketPoints) {
        return promotion.computeRewardsForPoints(tokenPoints, basketPoints);
    }

    // Provider centered api
    SPSEQSignature handleEarn(Promotion promotion, Basket basket, EarnPointsChoice earnPointsChoice, ProviderKeyPair providerKeyPair) {
        // Compute points that this basket will give to the user (not trusted, we compute it ourselves)
        var basketPoints = computeEarningsForBasket(promotion, basket);
        // Run Credit Earn protocol
        return incentiveSystem.generateEarnRequestResponse(promotion.promotionParameters, earnPointsChoice.earnRequest, basketPoints, providerKeyPair);
    }

    // Spend-Deduct
    SpendProviderOutput handleReward(Promotion promotion, Basket basket, RewardChoice rewardChoice, ProviderKeyPair providerKeyPair) {
        // Compute points that this basket will give to the user
        var basketPoints = computeEarningsForBasket(promotion, basket);
        // Compute ZKP for the user's choice of promotion reward, might depend on basketPoints to allow simultaneous earn and spend
        var spendDeductTree = rewardChoice.reward.generateRelationTree(basketPoints);
        // [Nice addition: basket contents might be enough to satisfy reward condition, in this case evaluate tree, and optionally run simple Earn for remaining points]
        var tid = basket.getBasketId(pp.getBg().getZn());
        // Run SpendDeduct
        return incentiveSystem.generateSpendRequestResponse(promotion.promotionParameters, rewardChoice.spendRequest, providerKeyPair, tid, spendDeductTree);
    }
}