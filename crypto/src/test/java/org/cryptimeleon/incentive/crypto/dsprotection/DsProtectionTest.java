package org.cryptimeleon.incentive.crypto.dsprotection;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendHelper;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;


public class DsProtectionTest {
    private static IncentivePublicParameters pp;
    private static IncentiveSystem incentiveSystem;
    private static PromotionParameters promotionParameters;
    private static ProviderKeyPair pkp;
    private static UserKeyPair ukp;
    private TestDatabaseHandler dbHandler;

    @BeforeAll
    static void setup() {
        pp = IncentiveSystem.setup(256, Setup.BilinearGroupChoice.Debug);
        incentiveSystem = new IncentiveSystem(pp);
        promotionParameters = IncentiveSystem.generatePromotionParameters(1);
        pkp = incentiveSystem.generateProviderKeys();
        ukp = incentiveSystem.generateUserKeys();
    }


    @BeforeEach
    void setupDBHandler() {
        dbHandler = new TestDatabaseHandler();
    }

    @Test
    void singleSpendOperation() {
        Token token = genToken(promotionParameters);
        Zn.ZnElement tid = pp.getBg().getZn().getUniformlyRandomElement(); // Public
        SpendResult spendResult = simulateSpendDeduct(promotionParameters, token, tid);

        incentiveSystem.dbSync(tid, spendResult.getDsid(), spendResult.doubleSpendingTag, BigInteger.ONE, dbHandler);
        assertThat(dbHandler.containsTokenNode(spendResult.getDsid())).isTrue();
        assertThat(dbHandler.getTokenCount()).isEqualTo(1);
        assertThat(dbHandler.getTransactionCount()).isEqualTo(1);
        assertThat(dbHandler.getDsTagCount()).isEqualTo(1);
        assertThat(dbHandler.getUserInfoCount()).isEqualTo(0);
    }

    @Test
    void doubleSpend() {
        Token token = genToken(promotionParameters);
        Zn.ZnElement tid = pp.getBg().getZn().getUniformlyRandomElement();
        SpendResult spendResult = simulateSpendDeduct(promotionParameters, token, tid);
        Zn.ZnElement tidDs = pp.getBg().getZn().getUniformlyRandomElement();
        SpendResult spendResultDs = simulateSpendDeduct(promotionParameters, token, tidDs);

        incentiveSystem.dbSync(tid, spendResult.getDsid(), spendResult.doubleSpendingTag, BigInteger.ONE, dbHandler);
        incentiveSystem.dbSync(tidDs, spendResultDs.getDsid(), spendResultDs.doubleSpendingTag, BigInteger.ONE, dbHandler);

        assertThat(dbHandler.getUserInfoCount()).isEqualTo(2);
    }

    @Test
    void tripleSpend() {
        Token token = genToken(promotionParameters);
        Zn.ZnElement tid = pp.getBg().getZn().getUniformlyRandomElement(); // Public
        SpendResult spendResult = simulateSpendDeduct(promotionParameters, token, tid);
        Zn.ZnElement tidDs = pp.getBg().getZn().getUniformlyRandomElement();
        SpendResult spendResultDs = simulateSpendDeduct(promotionParameters, token, tidDs);
        Zn.ZnElement tidDss = pp.getBg().getZn().getUniformlyRandomElement();
        SpendResult spendResultDss = simulateSpendDeduct(promotionParameters, token, tidDss);

        incentiveSystem.dbSync(tid, spendResult.getDsid(), spendResult.doubleSpendingTag, BigInteger.ONE, dbHandler);
        incentiveSystem.dbSync(tidDs, spendResultDs.getDsid(), spendResultDs.doubleSpendingTag, BigInteger.ONE, dbHandler);
        incentiveSystem.dbSync(tidDss, spendResultDss.getDsid(), spendResultDss.doubleSpendingTag, BigInteger.ONE, dbHandler);

        assertThat(dbHandler.getUserInfoCount()).isEqualTo(3);
    }


    private Token genToken(PromotionParameters promotionParameters) {
        BigInteger TOKEN_INITIAL_VALUE = BigInteger.valueOf(1000); // Large enough to allow all spend transactions
        return Helper.generateToken(pp, ukp, pkp, promotionParameters, Vector.of(TOKEN_INITIAL_VALUE));
    }

    private SpendResult simulateSpendDeduct(PromotionParameters promotionParameters, Token token, Zn.ZnElement tid) {
        BigInteger SPEND_COST = BigInteger.ONE;
        Vector<BigInteger> pointsAfterSpend = token.getPoints().map(RingElement::asInteger).map(p -> p.subtract(SPEND_COST));
        SpendDeductTree testSpendDeductTree = SpendHelper.generateSimpleTestSpendDeductTree(pp, promotionParameters, pkp.getPk(), Vector.of(SPEND_COST));
        SpendRequest spendRequest = incentiveSystem.generateSpendRequest(promotionParameters, token, pkp.getPk(), pointsAfterSpend, ukp, tid, testSpendDeductTree);
        DeductOutput deductOutput = incentiveSystem.generateSpendRequestResponse(promotionParameters, spendRequest, pkp, tid, testSpendDeductTree, tid);
        Token tokenAfterSpend = incentiveSystem.handleSpendRequestResponse(promotionParameters, deductOutput.getSpendResponse(), spendRequest, token, pointsAfterSpend, pkp.getPk(), ukp);
        return new SpendResult(deductOutput.getDstag(), tokenAfterSpend, spendRequest.getDsid());
    }

    @Value
    @AllArgsConstructor
    static
    class SpendResult {
        DoubleSpendingTag doubleSpendingTag;
        Token token;
        GroupElement dsid;
    }
}

