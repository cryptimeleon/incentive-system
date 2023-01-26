package org.cryptimeleon.incentive.promotion;

import org.cryptimeleon.incentive.crypto.crypto.TestSuite;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion;

import java.util.List;
import java.util.UUID;

public class TestSuiteWithPromotion extends TestSuite {
    static public final Promotion promotion = new HazelPromotion(
            HazelPromotion.generatePromotionParameters(),
            "First Test Promotion",
            "First Test Description",
            List.of(
                    new HazelTokenUpdate(UUID.randomUUID(), "Reward", new RewardSideEffect("Yay"), 2),
                    new HazelTokenUpdate(UUID.randomUUID(), "Some other reward", new RewardSideEffect("Even more Yay!"), 5)
            ),
            "Test");
    static public final Promotion alternativePromotion = new StreakPromotion(
            HazelPromotion.generatePromotionParameters(),
            "Second Test Promotion",
            "Second Test Description",
            List.of(new HazelTokenUpdate(UUID.randomUUID(), "Reward", new RewardSideEffect("Yay"), 2)),
            7);
}
