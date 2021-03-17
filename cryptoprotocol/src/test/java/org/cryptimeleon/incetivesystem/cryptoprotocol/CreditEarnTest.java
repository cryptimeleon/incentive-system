package org.cryptimeleon.incetivesystem.cryptoprotocol;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.EarnRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreditEarnTest {

    @Test
    void testSuccessFullCreditEarn() {
        var pp = IncentiveSystem.setup();
        var incentiveSystem = new IncentiveSystem(pp);
        var providerKeyPair = incentiveSystem.generateProviderKeys();
        var userKeyPair = incentiveSystem.generateUserKeys();


        // Create a dummy token.
        // This should be replaced by the actual methods that handle tokens when they are implemented.
        var g1 = pp.getBg().getG1();
        var zp = pp.getBg().getZn();
        var vectorH = providerKeyPair.getPk().getH();

        var s = zp.getUniformlyRandomNonzeroElement(); // TODO this should become part of the methods using a PRF

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
                .op(pp.getH7().pow(t));
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

        var earnAmount = 7;
        System.out.println("start earn request");
        var earnRequest = incentiveSystem.generateEarnRequest(token, providerKeyPair.getPk(), s);

        // Test representation of earnRequest
        var restoredEarnRequest = new EarnRequest(earnRequest.getRepresentation(), pp);
        assertEquals(earnRequest, restoredEarnRequest);

        System.out.println("start earn response");
        var signature = incentiveSystem.generateEarnRequestResponse(earnRequest, earnAmount, providerKeyPair);

        // Test representation of signature (to be sure)
        var restoredSignature = new SPSEQSignature(signature.getRepresentation(), pp.getBg().getG1(), pp.getBg().getG2() );
        assertEquals(signature, restoredSignature);

        System.out.println("handle earn response");
        var newToken = incentiveSystem.handleEarnRequestResponse(earnRequest, signature, earnAmount, token, userKeyPair, providerKeyPair.getPk(), s);

        System.out.println("Done");

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