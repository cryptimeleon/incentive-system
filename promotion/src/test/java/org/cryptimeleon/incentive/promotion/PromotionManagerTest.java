package org.cryptimeleon.incentive.promotion;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.promotion.model.EarnPointsChoice;
import org.cryptimeleon.incentive.promotion.model.RewardChoice;
import org.cryptimeleon.incentive.promotion.promotions.NutellaPromotion;
import org.cryptimeleon.incentive.promotion.reward.NutellaReward;
import org.cryptimeleon.incentive.promotion.reward.Reward;
import org.cryptimeleon.incentive.promotion.reward.RewardSideEffect;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromotionManagerTest {

    @Test
    void testManagerEarnWithNutellaPromotion() {
        IncentivePublicParameters publicParameters = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug);
        IncentiveSystem incentiveSystem = new IncentiveSystem(publicParameters);
        UserKeyPair userKeyPair = Setup.userKeyGen(publicParameters);
        ProviderKeyPair providerKeyPair = Setup.providerKeyGen(publicParameters);

        PromotionManager promotionManager = new PromotionManager(incentiveSystem);
        List<Reward> rewards = List.of(new NutellaReward(4, UUID.randomUUID(), new RewardSideEffect("Free Nutella")));
        NutellaPromotion nutellaPromotion = new NutellaPromotion(NutellaPromotion.generatePromotionParameters(), rewards);
        Basket basket = new Basket(UUID.randomUUID(), List.of(
                new BasketItem(UUID.randomUUID(), "Nutella", 400, 2),
                new BasketItem(UUID.randomUUID(), "Potatoes", 50, 1)
        ));

        Token token = Helper.generateToken(publicParameters, userKeyPair, providerKeyPair, nutellaPromotion.promotionParameters);
        EarnRequest earnRequest = incentiveSystem.generateEarnRequest(token, providerKeyPair.getPk(), userKeyPair);

        // User side computation of points to earn, fully deterministic
        Vector<BigInteger> pointsToEarn = promotionManager.computeEarningsForBasket(nutellaPromotion, basket);

        // Provider side computation of points to earn
        SPSEQSignature earnResponse = promotionManager.handleEarn(nutellaPromotion, basket, new EarnPointsChoice(earnRequest), providerKeyPair);

        // Compute updated token
        Token updatedToken = incentiveSystem.handleEarnRequestResponse(nutellaPromotion.promotionParameters, earnRequest, earnResponse, pointsToEarn, token, providerKeyPair.getPk(), userKeyPair);
        assertEquals(2, updatedToken.getPoints().get(0).asInteger().intValueExact());
    }

    @Test
    void testManagerRewardWithNutellaPromotion() {
        IncentivePublicParameters publicParameters = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug);
        IncentiveSystem incentiveSystem = new IncentiveSystem(publicParameters);
        UserKeyPair userKeyPair = Setup.userKeyGen(publicParameters);
        ProviderKeyPair providerKeyPair = Setup.providerKeyGen(publicParameters);

        PromotionManager promotionManager = new PromotionManager(incentiveSystem);
        List<Reward> rewards = List.of(new NutellaReward(4, UUID.randomUUID(), new RewardSideEffect("Free Nutella")));
        NutellaPromotion nutellaPromotion = new NutellaPromotion(NutellaPromotion.generatePromotionParameters(), rewards);
        Basket basket = new Basket(UUID.randomUUID(), List.of(
                new BasketItem(UUID.randomUUID(), "Nutella", 400, 2),
                new BasketItem(UUID.randomUUID(), "Potatoes", 50, 1)
        ));

        Token token = Helper.generateToken(publicParameters, userKeyPair, providerKeyPair, nutellaPromotion.promotionParameters, Vector.of(BigInteger.valueOf(3L)));

        // This happens on user side
        // Compute value of basket
        Vector<BigInteger> basketPoints = promotionManager.computeEarningsForBasket(nutellaPromotion, basket);
        // Compute list of qualified rewards
        List<Reward> rewardList = promotionManager.computeRewardsForPoints(nutellaPromotion, token.getPoints().map(RingElement::asInteger), basketPoints);
        // User chooses reward
        Reward chosenReward = rewardList.get(0);
        // User retrieves a valid newPoints vector for this reward
        Vector<BigInteger> newPoints = chosenReward.computeSatisfyingNewPointsVector(token.getPoints().map(RingElement::asInteger), basketPoints).orElseThrow();
        // [Sanity check, user needs the basket points to satisfy the promotion's requirements]
        assertEquals(Optional.empty(), chosenReward.computeSatisfyingNewPointsVector(token.getPoints().map(RingElement::asInteger), Vector.of(BigInteger.ZERO)));
        // Construct ZKP based on public basket points (If I have two nutella in my basket, I only need to spend two from the token)
        SpendDeductTree spendDeductTree = promotionManager.generateSpendDeductTree(nutellaPromotion, chosenReward, basketPoints);
        // Compute spend request
        SpendRequest spendRequest = incentiveSystem.generateSpendRequest(nutellaPromotion.promotionParameters, token, providerKeyPair.getPk(), newPoints, userKeyPair, basket.getBasketId(publicParameters.getBg().getZn()), spendDeductTree);

        // This happens on provider side
        SpendProviderOutput spendProviderOutput = promotionManager.handleReward(nutellaPromotion, basket, new RewardChoice(spendRequest, chosenReward), providerKeyPair);

        // This happens on user side
        Token updatedToken = incentiveSystem.handleSpendRequestResponse(nutellaPromotion.promotionParameters, spendProviderOutput.getSpendResponse(), spendRequest, token, newPoints, providerKeyPair.getPk(), userKeyPair);
        assertEquals(1, updatedToken.getPoints().get(0).asInteger().intValueExact());
        assertEquals(new RewardSideEffect("Free Nutella"), chosenReward.getSideEffect());
    }
}