package org.cryptimeleon.incentive.crypto.dsprotection;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendHelper;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;


public class DsProtectionTest {
    private static PromotionParameters promotionParameters = IncentiveSystem.generatePromotionParameters(1);
    private TestDatabaseHandler dbHandler;

    @BeforeEach
    void setupDBHandler() {
        dbHandler = new TestDatabaseHandler();
    }

    @Test
    void singleSpendOperation() {
        Token token = genToken(promotionParameters);

        spendTokenUniformTidAndDbSync(token);

        assertThat(dbHandler.getTransactionCount()).isEqualTo(1);
        assertThat(dbHandler.getInvalidTransactionCount()).isEqualTo(0);
    }

    @Test
    void doubleSpend() {
        Token token = genToken(promotionParameters);

        spendTokenUniformTidAndDbSync(token);
        spendTokenUniformTidAndDbSync(token);

        assertThat(dbHandler.getTransactionCount()).isEqualTo(2);
        assertThat(dbHandler.getInvalidTransactionCount()).isEqualTo(1);
    }

    @Test
    void tripleSpend() {
        Token token = genToken(promotionParameters);

        spendTokenUniformTidAndDbSync(token);
        spendTokenUniformTidAndDbSync(token);
        spendTokenUniformTidAndDbSync(token);

        assertThat(dbHandler.getTransactionCount()).isEqualTo(3);
        assertThat(dbHandler.getInvalidTransactionCount()).isEqualTo(2);
    }

    @Test
    void doubleSpendAfterValidChain() {
        int VALID_SPEND_CHAIN_LENGTH = 5;
        Token tokenToDoubleSpend = genToken(promotionParameters);
        spendAndDbSyncChain(VALID_SPEND_CHAIN_LENGTH, tokenToDoubleSpend);

        spendTokenUniformTidAndDbSync(tokenToDoubleSpend);

        assertThat(dbHandler.getTransactionCount()).isEqualTo(VALID_SPEND_CHAIN_LENGTH + 1);
        assertThat(dbHandler.getInvalidTransactionCount()).isEqualTo(1);
    }

    @Test
    void doubleSpendWithInvalidChain() {
        int INVALID_SPEND_CHAIN_LENGTH = 5;
        Token tokenToDoubleSpend = genToken(promotionParameters);
        spendTokenUniformTidAndDbSync(tokenToDoubleSpend);
        spendAndDbSyncChain(INVALID_SPEND_CHAIN_LENGTH, tokenToDoubleSpend);


        assertThat(dbHandler.getTransactionCount()).isEqualTo(INVALID_SPEND_CHAIN_LENGTH + 1);
        assertThat(dbHandler.getInvalidTransactionCount()).isEqualTo(INVALID_SPEND_CHAIN_LENGTH);
    }

    @Test
    void doubleSpendingTransactionAfterSuccessors() {
        Token tokenToDoubleSpend = genToken(promotionParameters);
        spendTokenUniformTidAndDbSync(tokenToDoubleSpend);

        // Double spending with dbsync executed later
        Zn.ZnElement tidDss = TestSuite.pp.getBg().getZn().getUniformlyRandomElement();
        SpendResult spendResultDss = simulateSpendDeduct(promotionParameters, tokenToDoubleSpend, tidDss);
        // Continue with double spending token, should be invalidated later
        spendTokenUniformTidAndDbSync(spendResultDss.tokenAfterSpend);
        // DBSync asynchronous for double spending
        TestSuite.incentiveSystem.dbSync(tidDss, spendResultDss.getDsid(), spendResultDss.doubleSpendingTag, "teddy bear", promotionParameters.getPromotionId(), dbHandler);

        assertThat(dbHandler.getTransactionCount()).isEqualTo(3);
        assertThat(dbHandler.getInvalidTransactionCount()).isEqualTo(2);
    }

    private void spendAndDbSyncChain(int chainLength, Token token) {
        Token newToken = token;
        for (int i = 0; i < chainLength; i++) {
            newToken = spendTokenUniformTidAndDbSync(newToken);
        }
    }

    private Token spendTokenUniformTidAndDbSync(Token token) {
        Zn.ZnElement tidDss = TestSuite.pp.getBg().getZn().getUniformlyRandomElement();
        SpendResult spendResultDss = simulateSpendDeduct(promotionParameters, token, tidDss);
        TestSuite.incentiveSystem.dbSync(tidDss, spendResultDss.getDsid(), spendResultDss.doubleSpendingTag, "teddy bear", promotionParameters.getPromotionId(), dbHandler);
        return spendResultDss.tokenAfterSpend;
    }

    private Token genToken(PromotionParameters promotionParameters) {
        BigInteger TOKEN_INITIAL_VALUE = BigInteger.valueOf(1000); // Large enough to allow all spend transactions
        return Helper.generateToken(TestSuite.pp, TestSuite.userKeyPair, TestSuite.providerKeyPair, promotionParameters, Vector.of(TOKEN_INITIAL_VALUE));
    }

    private SpendResult simulateSpendDeduct(PromotionParameters promotionParameters, Token token, Zn.ZnElement tid) {
        BigInteger SPEND_COST = BigInteger.ONE;
        Vector<BigInteger> pointsAfterSpend = token.getPoints().map(RingElement::asInteger).map(p -> p.subtract(SPEND_COST));
        SpendDeductTree testSpendDeductTree = SpendHelper.generateSimpleTestSpendDeductTree(TestSuite.pp, promotionParameters, TestSuite.providerKeyPair.getPk(), Vector.of(SPEND_COST));
        SpendRequest spendRequest = TestSuite.incentiveSystem.generateSpendRequest(promotionParameters, token, TestSuite.providerKeyPair.getPk(), pointsAfterSpend, TestSuite.userKeyPair, tid, testSpendDeductTree);
        DeductOutput deductOutput = TestSuite.incentiveSystem.generateSpendRequestResponse(promotionParameters, spendRequest, TestSuite.providerKeyPair, tid, testSpendDeductTree, tid);
        Token tokenAfterSpend = TestSuite.incentiveSystem.handleSpendRequestResponse(promotionParameters, deductOutput.getSpendResponse(), spendRequest, token, pointsAfterSpend, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair);
        return new SpendResult(deductOutput.getDstag(), tokenAfterSpend, spendRequest.getDsid());
    }

    @Value
    @AllArgsConstructor
    static
    class SpendResult {
        DoubleSpendingTag doubleSpendingTag;
        Token tokenAfterSpend;
        GroupElement dsid;
    }
}

