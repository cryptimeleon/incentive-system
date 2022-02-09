package org.cryptimeleon.incentive.promotion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.promotion.promotions.NutellaPromotion;
import org.cryptimeleon.incentive.promotion.reward.NutellaReward;
import org.cryptimeleon.incentive.promotion.reward.Reward;
import org.cryptimeleon.incentive.promotion.reward.RewardSideEffect;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class PromotionWorkflowTest {

    @Test
    void testManagerEarnWithNutellaPromotion() {
        List<Reward> rewards = List.of(new NutellaReward(4, "", UUID.randomUUID(), new RewardSideEffect("Free Nutella")));
        NutellaPromotion nutellaPromotion = new NutellaPromotion(NutellaPromotion.generatePromotionParameters(), "Test Promotion", "This is a Test Promotion", rewards, "nutella");
        Basket basket = new Basket(UUID.randomUUID(), List.of(
                new BasketItem(UUID.randomUUID(), "Nutella", 400, 2),
                new BasketItem(UUID.randomUUID(), "Potatoes", 50, 1)
        ));

        // User side computation of points to earn, fully deterministic
        Vector<BigInteger> pointsToEarn = nutellaPromotion.computeEarningsForBasket(basket);

        assertEquals(2, pointsToEarn.get(0).intValue());
    }

    @Test
    void testManagerRewardWithNutellaPromotion() {
        List<Reward> rewards = List.of(new NutellaReward(4, "", UUID.randomUUID(), new RewardSideEffect("Free Nutella")));
        NutellaPromotion nutellaPromotion = new NutellaPromotion(NutellaPromotion.generatePromotionParameters(), "Test Promotion", "This is another Test Promotion", rewards, "nutella");
        Basket basket = new Basket(UUID.randomUUID(), List.of(
                new BasketItem(UUID.randomUUID(), "Nutella", 400, 2),
                new BasketItem(UUID.randomUUID(), "Potatoes", 50, 1)
        ));
        Vector<BigInteger> tokenPoints = Vector.of(BigInteger.valueOf(3));

        // Compute value of basket
        Vector<BigInteger> basketPoints = nutellaPromotion.computeEarningsForBasket(basket);
        // Compute list of qualified rewards
        List<Reward> rewardList = nutellaPromotion.computeRewardsForPoints(tokenPoints, basketPoints);
        // User chooses reward
        Reward chosenReward = rewardList.get(0);
        // User retrieves a valid newPoints vector for this reward
        Vector<BigInteger> newPoints = chosenReward.computeSatisfyingNewPointsVector(tokenPoints, basketPoints).orElseThrow();
        // [Sanity check, user needs the basket points to satisfy the promotion's requirements]
        assertEquals(Optional.empty(), chosenReward.computeSatisfyingNewPointsVector(tokenPoints, Vector.of(BigInteger.ZERO)));

        // Construct ZKP based on public basket points (If I have two nutella in my basket, I only need to spend two from the token)
        SpendDeductTree spendDeductTree = chosenReward.generateRelationTree(basketPoints);
        assertTrue(spendDeductTree.isValidForPoints(tokenPoints, newPoints));
        assertEquals(1, newPoints.get(0).intValue());
        assertEquals(new RewardSideEffect("Free Nutella"), chosenReward.getRewardSideEffect());
    }
}
