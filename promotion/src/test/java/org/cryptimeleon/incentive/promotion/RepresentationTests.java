package org.cryptimeleon.incentive.promotion;

import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelReward;
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion;
import org.cryptimeleon.incentive.promotion.streak.StreakReward;
import org.cryptimeleon.incentive.promotion.vip.UpgradeVipReward;
import org.cryptimeleon.incentive.promotion.vip.VipPromotion;
import org.cryptimeleon.incentive.promotion.vip.VipReward;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that all Representables serialize and deserialize as expected.
 */
public class RepresentationTests {

    JSONConverter jsonConverter = new JSONConverter();

    @Test
    public void hazelPromotionRepresentationTest() {
        PromotionParameters promotionParameters = HazelPromotion.generatePromotionParameters();
        List<Reward> rewardList = List.of(new HazelReward(4, "Some reward", UUID.randomUUID(), new RewardSideEffect("Free Nutella")));
        HazelPromotion hazelPromotion = new HazelPromotion(promotionParameters, "Test Promotion", "This is some Test Promotion", rewardList, "nutella");
        HazelPromotion deserializedHazelPromotion = new HazelPromotion(hazelPromotion.getRepresentation());

        assertEquals(hazelPromotion, deserializedHazelPromotion);
    }

    @Test
    void hazelRewardRepresentationTest() {
        HazelReward hazelReward = new HazelReward(8, "", UUID.randomUUID(), new RewardSideEffect("Free Nutella"));
        HazelReward deserializedHazelReward = new HazelReward(hazelReward.getRepresentation());

        assertEquals(hazelReward, deserializedHazelReward);
    }

    @Test
    void vipPromotionRepresentationTest() {
        PromotionParameters promotionParameters = VipPromotion.generatePromotionParameters();
        VipPromotion vipPromotion = new VipPromotion(promotionParameters,
                "Test Promotion",
                "This is some Test Promotion",
                5, 10, 15,
                new RewardSideEffect("2% Discount"),
                new RewardSideEffect("5% Discount"),
                new RewardSideEffect("10% Discount")
        );
        VipPromotion deserializedVipPromotion = new VipPromotion(vipPromotion.getRepresentation());

        assertEquals(vipPromotion, deserializedVipPromotion);
    }

    @Test
    void streakPromotionRepresentationTest() {
        PromotionParameters promotionParameters = StreakPromotion.generatePromotionParameters();
        StreakPromotion streakPromotion = new StreakPromotion(
                promotionParameters,
                "Test Promotion",
                "This is some Test Promtion",
                List.of(),
                7
        );
        StreakPromotion deserializedStreakPromotion = new StreakPromotion(streakPromotion.getRepresentation());

        assertEquals(streakPromotion, deserializedStreakPromotion);
    }

    @Test
    void vipRewardRepresentationTest() {
        VipReward vipReward = new VipReward(2, UUID.randomUUID(), new RewardSideEffect("Some side effect"));
        VipReward deserializedVipReward = new VipReward(vipReward.getRepresentation());

        assertEquals(vipReward, deserializedVipReward);
    }

    @Test
    void vipUpgradeRewardRepresentationTest() {
        UpgradeVipReward upgradeVipReward = new UpgradeVipReward(2, 50, "Upgrade to 2", UUID.randomUUID());
        UpgradeVipReward deserializedUpgradeVipReward = new UpgradeVipReward(upgradeVipReward.getRepresentation());

        assertEquals(upgradeVipReward, deserializedUpgradeVipReward);
    }

    @Test
    void streakRewardRepresentationTest() {
        StreakReward streakReward = new StreakReward(UUID.randomUUID(), "Some Reward", new RewardSideEffect("Yay"), 7);
        StreakReward deserializedStreakReward = new StreakReward(streakReward.getRepresentation());

        assertEquals(streakReward, deserializedStreakReward);
    }


    @Test
    void rewardSideEffectRepresentationTest() {
        RewardSideEffect rewardSideEffect = new RewardSideEffect("Free Teddy");
        RewardSideEffect deserializedRewardSideEffect = new RewardSideEffect(rewardSideEffect.getRepresentation());
        assertEquals(rewardSideEffect, deserializedRewardSideEffect);
    }

    /**
     * Test that generic rewards can be deserialized to the corresponding classes.
     */
    @Test
    void allRewardsRepresentationTest() {
        UpgradeVipReward upgradeVipReward = new UpgradeVipReward(2, 50, "Upgrade to 2", UUID.randomUUID());

        // Needs to be casted to RepresentableRepresentation for the JsonConverter to correctly serialize it
        // getRepresentation() does not work, since the class is not encoded in the JSON string
        RepresentableRepresentation upgradeVipRewardReprRepr = new RepresentableRepresentation(upgradeVipReward);

        // Use these for REST
        String serializedRepresentation = jsonConverter.serialize(upgradeVipRewardReprRepr);

        Reward deserializedUpgradeVipReward = (Reward) ((RepresentableRepresentation) jsonConverter.deserialize(serializedRepresentation))
                .recreateRepresentable();
        assertEquals(upgradeVipReward, deserializedUpgradeVipReward);
    }
}
