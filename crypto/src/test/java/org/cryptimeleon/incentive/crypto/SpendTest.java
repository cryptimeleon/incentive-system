package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.callback.IStoreBasketRedeemedHandler;
import org.cryptimeleon.incentive.crypto.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendHelper;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.UUID;

public class SpendTest {
    IncentiveSystem incSys = TestSuite.incentiveSystem;
    UUID basketId = UUID.randomUUID();
    PromotionParameters promotionParameters = IncentiveSystem.generatePromotionParameters(2);
    Vector<BigInteger> pointsBeforeSpend = Vector.of(BigInteger.valueOf(10L), BigInteger.valueOf(0L));
    Vector<BigInteger> pointsAfterSpend = Vector.of(BigInteger.valueOf(6L), BigInteger.valueOf(0L));
    Vector<BigInteger> pointDifference = pointsBeforeSpend.zip(pointsAfterSpend, BigInteger::subtract);

    Token token = TestSuite.generateToken(promotionParameters, pointsBeforeSpend);

    @Test
    void successfulSpendTest() {
        SpendDeductTree spendDeductTree = SpendHelper.generateSimpleTestSpendDeductTree(promotionParameters, pointDifference);
        SpendCouponRequest spendCouponRequest = incSys.generateStoreSpendRequest(
                token,
                TestSuite.userKeyPair,
                pointsAfterSpend,
                TestSuite.providerKeyPair.getPk(),
                promotionParameters,
                basketId,
                spendDeductTree
        );
         SpendStoreOutput spendStoreOutput = incSys.generateSpendCouponAndIssueReward(
                 TestSuite.storeKeyPair,
                 TestSuite.providerKeyPair.getPk(),
                 basketId,
                 promotionParameters,
                 spendCouponRequest,
                 spendDeductTree,
                 new IStoreBasketRedeemedHandler() {
                     @Override
                     public boolean verifyAndStorePromotionIdAndHashForBasket(UUID basketId, BigInteger promotionId, byte[] hash) {
                         throw new RuntimeException();
                     }

                     @Override
                     public BasketRedeemedResult verifyAndRedeemBasket(UUID basketId, BigInteger promotionId, Zn.ZnElement gamma, SpendCouponSignature signature) {
                         return new IStoreBasketRedeemedHandler.BasketNotRedeemed();
                     }
                 }
         );
        SpendCouponSignature spendCouponSignature = spendStoreOutput.spendCouponSignature;
        Assertions.assertTrue(incSys.verifySpendCouponSignature(spendCouponRequest, spendCouponSignature, promotionParameters, basketId));
    }

    @Test
    void representationTests() {
        SpendDeductTree spendDeductTree = SpendHelper.generateSimpleTestSpendDeductTree(promotionParameters, pointDifference);
        SpendCouponRequest spendCouponRequest = incSys.generateStoreSpendRequest(
                token,
                TestSuite.userKeyPair,
                pointsAfterSpend,
                TestSuite.providerKeyPair.getPk(),
                promotionParameters,
                basketId,
                spendDeductTree
        );
        SpendStoreOutput spendStoreOutput = incSys.generateSpendCouponAndIssueReward(
                TestSuite.storeKeyPair,
                TestSuite.providerKeyPair.getPk(),
                basketId,
                promotionParameters,
                spendCouponRequest,
                spendDeductTree,
                new IStoreBasketRedeemedHandler() {
                    @Override
                    public boolean verifyAndStorePromotionIdAndHashForBasket(UUID basketId, BigInteger promotionId, byte[] hash) {
                        throw new RuntimeException();
                    }

                    @Override
                    public BasketRedeemedResult verifyAndRedeemBasket(UUID basketId, BigInteger promotionId, Zn.ZnElement gamma, SpendCouponSignature signature) {
                        return new IStoreBasketRedeemedHandler.BasketNotRedeemed();
                    }
                }
        );
        SpendCouponSignature spendCouponSignature = spendStoreOutput.spendCouponSignature;
        SpendClearingData spendClearingData = spendStoreOutput.spendClearingData;

        SpendCouponSignature deserializedSpendCouponSignature = new SpendCouponSignature(spendCouponSignature.getRepresentation());
        SpendClearingData deserializedSpendClearingData = new SpendClearingData(spendClearingData.getRepresentation(), incSys.pp, promotionParameters, spendDeductTree, TestSuite.providerKeyPair.getPk());
        SpendCouponRequest deserialzedSpendCouponRequest = new SpendCouponRequest(
                spendCouponRequest.getRepresentation(),
                incSys.pp,
                basketId,
                promotionParameters,
                TestSuite.providerKeyPair.getPk(),
                spendDeductTree
        );

        Assertions.assertEquals(spendCouponRequest, deserialzedSpendCouponRequest);
        Assertions.assertEquals(spendCouponSignature, deserializedSpendCouponSignature);
        Assertions.assertEquals(spendClearingData, deserializedSpendClearingData);
    }

}
