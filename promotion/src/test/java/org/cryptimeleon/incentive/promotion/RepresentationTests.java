package org.cryptimeleon.incentive.promotion;

import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.streak.RangeProofStreakTokenUpdate;
import org.cryptimeleon.incentive.promotion.streak.SpendStreakTokenUpdate;
import org.cryptimeleon.incentive.promotion.streak.StandardStreakTokenUpdate;
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion;
import org.cryptimeleon.incentive.promotion.vip.ProveVipTokenUpdate;
import org.cryptimeleon.incentive.promotion.vip.UpgradeVipZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.vip.VipPromotion;
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
        List<ZkpTokenUpdate> zkpTokenUpdateList = List.of(new HazelTokenUpdate(UUID.randomUUID(), "Some reward", new RewardSideEffect("Free Nutella"), 4));
        HazelPromotion hazelPromotion = new HazelPromotion(promotionParameters, "Test Promotion", "This is some Test Promotion", zkpTokenUpdateList, "nutella");
        HazelPromotion deserializedHazelPromotion = new HazelPromotion(hazelPromotion.getRepresentation());

        assertEquals(hazelPromotion, deserializedHazelPromotion);
    }

    @Test
    void hazelRewardRepresentationTest() {
        HazelTokenUpdate hazelReward = new HazelTokenUpdate(UUID.randomUUID(), "", new RewardSideEffect("Free Nutella"), 8);
        HazelTokenUpdate deserializedHazelReward = new HazelTokenUpdate(hazelReward.getRepresentation());

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
    void proveVipUpdateRepresentationTest() {
        ProveVipTokenUpdate vipTokenUpdate = new ProveVipTokenUpdate(UUID.randomUUID(), 2, new RewardSideEffect("Some side effect"));
        ProveVipTokenUpdate deserializedVipTokenUpdate = new ProveVipTokenUpdate(vipTokenUpdate.getRepresentation());

        assertEquals(vipTokenUpdate, deserializedVipTokenUpdate);
    }

    @Test
    void upgradeVipUpdateRepresentationTest() {
        UpgradeVipZkpTokenUpdate upgradeVipReward = new UpgradeVipZkpTokenUpdate(UUID.randomUUID(), "Upgrade to 2", 2, 50);
        UpgradeVipZkpTokenUpdate deserializedUpgradeVipReward = new UpgradeVipZkpTokenUpdate(upgradeVipReward.getRepresentation());

        assertEquals(upgradeVipReward, deserializedUpgradeVipReward);
    }

    @Test
    void standardStreakUpdateRepresentationTest() {
        StandardStreakTokenUpdate streakTokenUpdate = new StandardStreakTokenUpdate(UUID.randomUUID(), "Some Reward", new RewardSideEffect("Yay"), 7);
        StandardStreakTokenUpdate deserializedStreakTokenUpdate = new StandardStreakTokenUpdate(streakTokenUpdate.getRepresentation());

        assertEquals(streakTokenUpdate, deserializedStreakTokenUpdate);
    }

    @Test
    void rangeProofStreakUpdateRepresentationTest() {
        RangeProofStreakTokenUpdate streakTokenUpdate = new RangeProofStreakTokenUpdate(UUID.randomUUID(), "Some Reward", new RewardSideEffect("Yay"), 7, 20);
        RangeProofStreakTokenUpdate deserializedStreakTokenUpdate = new RangeProofStreakTokenUpdate(streakTokenUpdate.getRepresentation());

        assertEquals(streakTokenUpdate, deserializedStreakTokenUpdate);
    }

    @Test
    void spendStreakUpdateRepresentationTest() {
        SpendStreakTokenUpdate streakTokenUpdate = new SpendStreakTokenUpdate(UUID.randomUUID(), "Some Reward", new RewardSideEffect("Yay"), 7, 20);
        SpendStreakTokenUpdate deserializedStreakTokenUpdate = new SpendStreakTokenUpdate(streakTokenUpdate.getRepresentation());

        assertEquals(streakTokenUpdate, deserializedStreakTokenUpdate);
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
    void allZkpUpdatesRepresentationTest() {
        UpgradeVipZkpTokenUpdate upgradeVipReward = new UpgradeVipZkpTokenUpdate(UUID.randomUUID(), "Upgrade to 2", 2, 50);

        // Needs to be casted to RepresentableRepresentation for the JsonConverter to correctly serialize it
        // getRepresentation() does not work, since the class is not encoded in the JSON string
        RepresentableRepresentation upgradeVipRewardReprRepr = new RepresentableRepresentation(upgradeVipReward);

        // Use these for REST
        String serializedRepresentation = jsonConverter.serialize(upgradeVipRewardReprRepr);

        ZkpTokenUpdate deserializedUpgradeVipZkpTokenUpdate = (ZkpTokenUpdate) ((RepresentableRepresentation) jsonConverter.deserialize(serializedRepresentation))
                .recreateRepresentable();
        assertEquals(upgradeVipReward, deserializedUpgradeVipZkpTokenUpdate);
    }
}
