package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.model.EarnRequest;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the credit-earn protocol.
 */
public class CreditEarnTest {

    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Positive test for credit earn.
     */
    @Test
    void testSuccessFullCreditEarn() {
        logger.info("Setup test");
        var pp = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug);
        var incentiveSystem = new IncentiveSystem(pp);
        var providerKeyPair = incentiveSystem.generateProviderKeys();
        var userKeyPair = incentiveSystem.generateUserKeys();
        var earnAmount = BigInteger.valueOf(7);

        // Create a dummy token.
        // This should be replaced by the actual methods that handle tokens when they are implemented.
        var token = Helper.generateToken(pp, userKeyPair, providerKeyPair);

        assertTrue(pp.getSpsEq().verify(
                providerKeyPair.getPk().getPkSpsEq(),
                token.getSignature(),
                token.getCommitment0(),
                token.getCommitment1()
        ));

        logger.info("compute earn request");
        var earnRequest = incentiveSystem.generateEarnRequest(token, providerKeyPair.getPk(), userKeyPair);
        logger.info("represent earn request");
        var earnRequestRepresentation = earnRequest.getRepresentation();
        logger.info("parse earn request");
        var earnRequestParsed = new EarnRequest(earnRequestRepresentation, pp);
        logger.info("compute earn response");
        var signature = incentiveSystem.generateEarnRequestResponse(earnRequestParsed, earnAmount, providerKeyPair);
        logger.info("represent earn response");
        var signatureRepresentation = signature.getRepresentation();
        logger.info("parse earn response");
        var signatureParsed = new SPSEQSignature(signatureRepresentation, pp.getBg().getG1(), pp.getBg().getG2());
        logger.info("handle earn response");
        var newToken = incentiveSystem.handleEarnRequestResponse(earnRequest, signatureParsed, earnAmount, token, providerKeyPair.getPk(), userKeyPair);
        logger.info("retrieved new token");


        // Some representation tests. Outsourced for a better measure of performance
        // Test representation of earnRequest
        var restoredEarnRequest = new EarnRequest(earnRequest.getRepresentation(), pp);
        assertEquals(earnRequest, restoredEarnRequest);

        // Test representation of signature (to be sure)
        var restoredSignature = new SPSEQSignature(signature.getRepresentation(), pp.getBg().getG1(), pp.getBg().getG2());
        assertEquals(signature, restoredSignature);

        assertEquals(
                newToken.getPoints().getInteger(),
                token.getPoints().getInteger().add(earnAmount)
        );

        assertTrue(pp.getSpsEq().verify(
                providerKeyPair.getPk().getPkSpsEq(),
                newToken.getSignature(),
                newToken.getCommitment0(),
                newToken.getCommitment1()
        ));
    }
}