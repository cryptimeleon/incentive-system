package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.callback.IRegistrationCouponDBHandler;
import org.cryptimeleon.incentive.crypto.callback.IStorePublicKeyVerificationHandler;
import org.cryptimeleon.incentive.crypto.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.store.StoreKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendHelper;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Performs a full example run of all three protocols as in a real-world setting.
 * A new user joins the system using the Issue-Join protocol,
 * then earns some points using the Credit-Earn protocol
 * and performs some (valid as well as invalid) Spend operations.
 * Note: since no state is stored on either side at cryptoprotocol level, it makes no sense to test with multiple users here.
 */
public class IncentiveSystemTest {
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    IncentiveSystem incSys = TestSuite.incentiveSystem;
    Zn usedZn = incSys.getPp().getBg().getZn();
    StoreKeyPair skp = TestSuite.storeKeyPair;
    ProviderKeyPair pkp = TestSuite.providerKeyPair;
    UserKeyPair ukp = TestSuite.userKeyPair;

    @Test
    public void registrationTest() {
        UserPublicKey upk = ukp.getPk();
        String userInfo = "Name: Max Mustermann, ID-Number: 12345678";
        List<RegistrationCoupon> registrationCouponList = new ArrayList<>();
        IRegistrationCouponDBHandler iRegistrationCouponDBHandler = registrationCouponList::add;
        IStorePublicKeyVerificationHandler iStorePublicKeyVerificationHandler = (s) -> true;


        RegistrationCoupon registrationCoupon = incSys.signRegistrationCoupon(skp, upk, userInfo);
        SPSEQSignature registrationToken = incSys.verifyRegistrationCouponAndIssueRegistrationToken(
                pkp,
                registrationCoupon,
                iStorePublicKeyVerificationHandler,
                iRegistrationCouponDBHandler
        );

        assertThat(incSys.verifyRegistrationCoupon(registrationCoupon, (s)-> true)).isTrue();
        assertThat(registrationCouponList).hasSize(1);
        assertThat(incSys.verifyRegistrationToken(pkp.getPk(), registrationToken, registrationCoupon)).isTrue();

        // Some additional representation test here to avoid duplicate setup
        RegistrationCoupon deserializedRegistrationCoupon = new RegistrationCoupon(registrationCoupon.getRepresentation(), TestSuite.incentiveSystemRestorer);
        assertThat(registrationCoupon).isEqualTo(deserializedRegistrationCoupon);
    }

    @Test
    public void fullProtocolTestRun() {
        logger.info("Starting integration test of all three cryptographic protocols.");

        /*
         * incentive system setup and user+provider key generation
         */

        logger.info("Setup promotion parameters.");

        // generate promotion parameters
        var promotionParameters = IncentiveSystem.generatePromotionParameters(2);
        Vector<BigInteger> ignore = Util.getNullBigIntegerVector(2);
        Vector<BigInteger> ones = Util.getOneBigIntegerVector(2);
        Vector<BigInteger> zeros = Util.getZeroBigIntegerVector(2);

        /*
         * user joins system using issue-join protocol
         */

        logger.info("A new user joins the system.");

        // user generates join request
        var generateIssueJoinOutput = incSys.generateJoinRequest(
                pkp.getPk(),
                ukp
        );

        // serialize and deserialize join request to ensure serialization does not break anything
        var serializedJoinRequest = generateIssueJoinOutput.getJoinRequest().getRepresentation();
        var deserializedJoinRequest = new JoinRequest(serializedJoinRequest, incSys.getPp(), pkp.getPk());

        // provider handles join request and generates join response
        var joinResponse = incSys.generateJoinRequestResponse(promotionParameters, pkp, deserializedJoinRequest);

        // serialize and deserialize join response
        var serializedJoinResponse = joinResponse.getRepresentation();
        var deserializedJoinResponse = new JoinResponse(serializedJoinResponse, incSys.getPp());

        // user handles join response
        var initialToken = incSys.handleJoinRequestResponse(promotionParameters, pkp.getPk(), generateIssueJoinOutput, deserializedJoinResponse);

        /*
         * transaction 1: user tries to spend points with an empty token

        logger.info("Testing spend transaction with empty token.");

        // generate a fresh ID for the spend transaction
        var tid1 = usedZn.getUniformlyRandomElement();

        var spendAmount1 = Vector.of(BigInteger.ONE, BigInteger.ZERO);

        // ensure exception is thrown when user tries to generate spend request
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> incSys.generateSpendRequest(promotionParameters, initialToken, pkp.getPk(), spendAmount1, ukp, tid1)
        );

         * transaction 2: user earns 20 points
         */

        logger.info("Initialize Credit-Earn execution which grants user 20 and 5 points.");
        var earnAmount1 = Vector.of(BigInteger.valueOf(20L), BigInteger.valueOf(5L));

        // user generates earn request
        var earnRequest1 = incSys.generateEarnRequest(initialToken, pkp.getPk(), ukp);

        // serialize and deserialize earn request to ensure serialization does not break anything
        var serializedEarnRequest1 = earnRequest1.getRepresentation();
        var deserializedEarnRequest1 = new EarnRequest(serializedEarnRequest1, incSys.getPp());

        // provider handles earn request and generates earn response
        var earnResponse1 = incSys.generateEarnRequestResponse(promotionParameters, deserializedEarnRequest1, earnAmount1, pkp);

        // serialize and deserialize earn response to ensure serialization does not break anything
        var serializedEarnResponse1 = earnResponse1.getRepresentation();
        var deserializedEarnResponse1 = new SPSEQSignature(serializedEarnResponse1, incSys.getPp().getBg().getG1(), incSys.getPp().getBg().getG2());

        // user handles earn response
        var updatedToken = incSys.handleEarnRequestResponse(promotionParameters, deserializedEarnRequest1, deserializedEarnResponse1, earnAmount1, initialToken, pkp.getPk(), ukp);

        // ensure user token contains 20 points
        Assertions.assertArrayEquals(earnAmount1.stream().toArray(), updatedToken.getPoints().stream().map(RingElement::asInteger).toArray());

        /*
         * transaction 3: user tries to spend 23 points
         *
         * TODO add token condition proof for testing this
         * TODO after doing so, add hashedClaim PRFtoZn value as discussed in issue #75

        logger.info("Testing failing spend transaction with non-empty token.");

        // generate a fresh ID for the spend transaction
        var tid2 = usedZn.getUniformlyRandomElement();

        // define spend amount
        var spendAmount2 = Vector.of(BigInteger.valueOf(10L), BigInteger.valueOf(7L));

        final Token spendTransaction2Token = updatedToken;

        // ensure exception is thrown when user tries to generate spend request
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            incSys.generateSpendRequest(promotionParameters, spendTransaction2Token, pkp.getPk(), spendAmount2, ukp, tid2); // this call does not change updatedToken
        });
         */

        /*
         * transaction 4: user spends points
         */

        logger.info("Testing successful spend transaction.");

        // generate a fresh ID for the spend transaction
        var tid3 = usedZn.getUniformlyRandomElement();

        // define spend amount
        var newPointsAmount3 = Vector.of(BigInteger.valueOf(10L), BigInteger.valueOf(2L));
        Token finalUpdatedToken = updatedToken;
        Vector<BigInteger> spendAmount = Vector.generatePlain(
                i -> finalUpdatedToken.getPoints().get(i).asInteger().subtract(newPointsAmount3.get(i)),
                newPointsAmount3.length()
        );
        SpendDeductTree spendDeductTestZkp = SpendHelper.generateSimpleTestSpendDeductTree(promotionParameters, spendAmount);

        // user generates spend request
        SpendRequest spendRequest3 = incSys.generateSpendRequest(promotionParameters, updatedToken, pkp.getPk(), newPointsAmount3, ukp, tid3, spendDeductTestZkp);
        Vector<BigInteger> negatedSpendAmount = Vector.fromStreamPlain(spendAmount.stream().map(BigInteger::negate));

        // serialize and deserialize spend request to ensure that serialization does not break anything
        var serializedSpendRequest3 = spendRequest3.getRepresentation();
        FiatShamirProofSystem spendDeductProofSystem = new FiatShamirProofSystem(
                new SpendDeductBooleanZkp(
                        new TokenUpdateLeaf("TokenUpdateLeaf", zeros, ignore, ones, negatedSpendAmount),
                        incSys.getPp(), promotionParameters, pkp.getPk()));
        // using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
        var deserializedSpendRequest3 = new SpendRequest(serializedSpendRequest3, incSys.getPp(), spendDeductProofSystem, tid3, tid3);

        // provider handles spend request and generates spend response and information required for double-spending protection (which is discarded on the fly, since not needed in this test)
        // using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
        var spendResponse3 = incSys.generateSpendRequestResponse(promotionParameters, deserializedSpendRequest3, pkp, tid3, spendDeductTestZkp, tid3).getSpendResponse();

        // serialize and deserialize spend request to ensure that serialization does not break anything
        var serializedSpendResponse3 = spendResponse3.getRepresentation();
        var deserializedSpendResponse3 = new SpendResponse(serializedSpendResponse3, usedZn, incSys.getPp().getSpsEq());

        // user handles spend response
        updatedToken = incSys.handleSpendRequestResponse(promotionParameters, deserializedSpendResponse3, deserializedSpendRequest3, updatedToken, newPointsAmount3, pkp.getPk(), ukp);

        Assertions.assertArrayEquals(
                newPointsAmount3.stream().toArray(),
                updatedToken.getPoints().stream().map(RingElement::asInteger).toArray()
        );

        /*
         * transaction 5: user earns 334231 points
         */

        logger.info("Initialize Credit-Earn execution which grants user 334231 and 42 points.");
        var earnAmount5 = Vector.of(BigInteger.valueOf(334231L), BigInteger.valueOf(42L));

        // user generates earn request
        var earnRequest2 = incSys.generateEarnRequest(updatedToken, pkp.getPk(), ukp);

        // serialize and deserialize earn request to ensure serialization does not break anything
        var serializedEarnRequest2 = earnRequest2.getRepresentation();
        var deserializedEarnRequest2 = new EarnRequest(serializedEarnRequest2, incSys.getPp());

        // provider handles earn request and generates earn response
        var earnResponse2 = incSys.generateEarnRequestResponse(promotionParameters, deserializedEarnRequest2, earnAmount5, pkp);

        // serialize and deserialize earn response to ensure serialization does not break anything
        var serializedEarnResponse2 = earnResponse2.getRepresentation();
        var deserializedEarnResponse2 = new SPSEQSignature(serializedEarnResponse2, incSys.getPp().getBg().getG1(), incSys.getPp().getBg().getG2());

        // user handles earn response
        updatedToken = incSys.handleEarnRequestResponse(promotionParameters, deserializedEarnRequest2, deserializedEarnResponse2, earnAmount5, updatedToken, pkp.getPk(), ukp);

        Assertions.assertEquals(
                Vector.of(BigInteger.valueOf(334241L), BigInteger.valueOf(44L)),
                updatedToken.getPoints().map(RingElement::asInteger)
        );

        logger.info("Done testing protocols.");
    }
}
