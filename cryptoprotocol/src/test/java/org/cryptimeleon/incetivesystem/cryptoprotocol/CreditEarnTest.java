package org.cryptimeleon.incetivesystem.cryptoprotocol;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.EarnRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token;
import org.junit.jupiter.api.Test;

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
        var pp = IncentiveSystem.setup();
        var incentiveSystem = new IncentiveSystem(pp);
        var providerKeyPair = incentiveSystem.generateProviderKeys();
        var userKeyPair = incentiveSystem.generateUserKeys();
        var earnAmount = 7;

        // Create a dummy token.
        // This should be replaced by the actual methods that handle tokens when they are implemented.
        var zp = pp.getBg().getZn();
        var vectorH = providerKeyPair.getPk().getH();

        var s = zp.getUniformlyRandomNonzeroElement(); // TODO this should become part of the methods using a PRF

        // Manually create a token since issue-join is not yet implemented
        logger.info("Build token");
        var encryptionSecretKey = zp.getUniformlyRandomNonzeroElement();
        var dsrd1 = zp.getUniformlyRandomElement();
        var dsrd2 = zp.getUniformlyRandomElement();
        var z = zp.getUniformlyRandomElement();
        var t = zp.getUniformlyRandomElement();
        var points = 0;
        var pointsZp = zp.valueOf(points);
        var c1 = vectorH.get(0).pow(userKeyPair.getSk().getUsk())
                .op(vectorH.get(1).pow(encryptionSecretKey))
                .op(vectorH.get(2).pow(dsrd1))
                .op(vectorH.get(3).pow(dsrd2))
                .op(vectorH.get(4).pow(pointsZp))
                .op(vectorH.get(5).pow(z))
                .op(pp.getH7().pow(t)).compute();
        var c2 = pp.getG1();

        var token = new Token(
                c1,
                c2,
                encryptionSecretKey,
                dsrd1,
                dsrd2,
                z,
                t,
                pointsZp,
                (SPSEQSignature) pp.getSpsEq().sign(
                        providerKeyPair.getSk().getSkSpsEq(),
                        c1,
                        c2
                )
        );

        assertTrue(pp.getSpsEq().verify(
                providerKeyPair.getPk().getPkSpsEq(),
                token.getSignature(),
                token.getC1(),
                token.getC2()
        ));

        logger.info("compute earn request");
        var earnRequest = incentiveSystem.generateEarnRequest(token, providerKeyPair.getPk(), s);
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
        var newToken = incentiveSystem.handleEarnRequestResponse(earnRequest, signatureParsed, earnAmount, token, userKeyPair, providerKeyPair.getPk(), s);
        logger.info("retrieved new token");


        // Some representation tests. Outsourced for a better measure of performance
        // Test representation of earnRequest
        var restoredEarnRequest = new EarnRequest(earnRequest.getRepresentation(), pp);
        assertEquals(earnRequest, restoredEarnRequest);

        // Test representation of signature (to be sure)
        var restoredSignature = new SPSEQSignature(signature.getRepresentation(), pp.getBg().getG1(), pp.getBg().getG2() );
        assertEquals(signature, restoredSignature);

        assertEquals(
                newToken.getPoints().getInteger().longValue(),
                token.getPoints().getInteger().longValue() + earnAmount
        );

        assertTrue(pp.getSpsEq().verify(
                providerKeyPair.getPk().getPkSpsEq(),
                newToken.getSignature(),
                newToken.getC1(),
                newToken.getC2()
        ));
    }
}