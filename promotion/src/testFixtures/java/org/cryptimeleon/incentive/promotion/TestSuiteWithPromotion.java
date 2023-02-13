package org.cryptimeleon.incentive.promotion;

import org.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class TestSuiteWithPromotion extends TestSuite {

    // Spend stuff
    public final static int spendCost = 2;
    public final static UUID spendTokenUpdateId = UUID.randomUUID();
    public final static Vector<BigInteger> pointsBeforeSpend = Vector.of(BigInteger.valueOf(10L));
    public final static Vector<BigInteger> difference = Vector.of(BigInteger.valueOf(spendCost));
    public final static Vector<BigInteger> pointsAfterSpend = pointsBeforeSpend.zip(difference, BigInteger::subtract);
    public final static HazelTokenUpdate spendTokenUpdate = new HazelTokenUpdate(spendTokenUpdateId, "Reward", new RewardSideEffect("Yay"), spendCost);
    static public final Promotion promotion = new HazelPromotion(
            HazelPromotion.generatePromotionParameters(),
            "First Test Promotion",
            "First Test Description",
            List.of(
                    spendTokenUpdate,
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
