package org.cryptimeleon.incentive.promotion;

import org.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class TestSuiteWithPromotion extends TestSuite {

    // Spend stuff
    public final static int spendCost = 5;
    public final static UUID spendTokenUpdateId = UUID.randomUUID();
    public final static HazelTokenUpdate spendTokenUpdate = new HazelTokenUpdate(spendTokenUpdateId, "Reward", new RewardSideEffect("Yay"), spendCost);
    public static final String KEYWORD = "Matching String";
    static public final Promotion promotion = new HazelPromotion(
            HazelPromotion.generatePromotionParameters(),
            "First Test Promotion",
            "First Test Description",
            List.of(
                    spendTokenUpdate,
                    new HazelTokenUpdate(UUID.randomUUID(), "Some other reward", new RewardSideEffect("Even more Yay!"), 5)
            ),
            KEYWORD);
    static public final Promotion alternativePromotion = new StreakPromotion(
            HazelPromotion.generatePromotionParameters(),
            "Second Test Promotion",
            "Second Test Description",
            List.of(new HazelTokenUpdate(UUID.randomUUID(), "Reward", new RewardSideEffect("Yay"), 2)),
            7);


    // Some basket to use in tests
    static final private List<BasketItem> basketContent = List.of(
            new BasketItem(UUID.randomUUID().toString(), "Hazelnut Spread", 200, 5),
            new BasketItem(UUID.randomUUID().toString(), "Large Hazelnut Spread", 100, 3),
            new BasketItem(UUID.randomUUID().toString(), "Item containing " + KEYWORD, 100, 2)
            );
    static public final Basket basket = new Basket(UUID.randomUUID(), basketContent);
    static public final Basket basketButWithDifferentId = new Basket(UUID.randomUUID(), basketContent);
    public final static Vector<BigInteger> pointsBeforeSpend = Vector.of(BigInteger.valueOf(10L));
    static public final Vector<BigInteger> basketPoints = promotion.computeEarningsForBasket(basket);
    public static final  ZkpTokenUpdateMetadata metadata = promotion.generateMetadataForUpdate();
    public final static Vector<BigInteger> pointsAfterSpend = TestSuiteWithPromotion.spendTokenUpdate.computeSatisfyingNewPointsVector(pointsBeforeSpend, basketPoints, metadata).get();
    public final static Vector<BigInteger> difference = pointsBeforeSpend.zip(pointsAfterSpend, BigInteger::subtract);
    public final static SpendDeductTree tree = TestSuiteWithPromotion.spendTokenUpdate.generateRelationTree(basketPoints, metadata);
    public final static UniqueByteRepresentable context = ContextManager.computeContext(TestSuiteWithPromotion.spendTokenUpdate.getTokenUpdateId(), basketPoints, metadata);

    // Some stuff with empty baskets
    static public final Basket emptyBasket = new Basket(
            UUID.randomUUID(),
            List.of()
    );
    // For DSP testing
    static public final Basket emptyBasketTwo = new Basket(
            UUID.randomUUID(),
            List.of()
    );
    public final static Vector<BigInteger> emptyBasketPointsBeforeSpend = Vector.of(BigInteger.valueOf(13L));
    static public final Vector<BigInteger> emptyBasketPoints = promotion.computeEarningsForBasket(emptyBasket);
    public final static Vector<BigInteger> emptyBasketPointsAfterSpend = TestSuiteWithPromotion.spendTokenUpdate.computeSatisfyingNewPointsVector(pointsBeforeSpend, emptyBasketPoints, metadata).get();
    public final static Vector<BigInteger> emptyBasketDifference = emptyBasketPointsBeforeSpend.zip(emptyBasketPointsAfterSpend, BigInteger::subtract);
}
