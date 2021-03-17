package org.cryptimeleon.incetivesystem.cryptoprotocol;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.junit.jupiter.api.Test;

import static org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem.vectorToMessageBlock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreditEarnTest {

    @Test
    void testSuccessFullCreditEarn() {
        var pp = IncentiveSystem.setup();
        var incentiveSystem = new IncentiveSystem(pp);
        var providerKeyPair = incentiveSystem.generateProviderKeys();
        var userKeyPair = incentiveSystem.generateUserKeys();

        var s = pp.getBg().getZn().getUniformlyRandomNonzeroElement(); // TODO this should become part of the methods using a PRF

        // Create a dummy token.
        // This should be replaced by the actual methods that handle tokens when they are implemented.
        var g1 = pp.getBg().getG1();
        var zp = pp.getBg().getZn();

        var encryptionSecretKey = zp.getUniformlyRandomNonzeroElement();
        var dsrd1 = zp.getUniformlyRandomElement();
        var dsrd2 = zp.getUniformlyRandomElement();
        var z = zp.getUniformlyRandomElement();
        var t = zp.getUniformlyRandomElement();
        var points = 0;
        var pointsZp = zp.valueOf(points);
        var Hs = providerKeyPair.getPk().getH();
        var C = new GroupElementVector(
                Hs.get(0).pow(userKeyPair.getSk().getUsk())
                        .op(Hs.get(1).pow(encryptionSecretKey))
                        .op(Hs.get(2).pow(dsrd1))
                        .op(Hs.get(3).pow(dsrd2))
                        .op(Hs.get(4).pow(pointsZp))
                        .op(Hs.get(5).pow(z))
                        .op(pp.getH7().pow(t)),
                pp.getG1()
        );

        var token = new Token(
                C,
                encryptionSecretKey,
                dsrd1,
                dsrd2,
                z,
                t,
                pointsZp,
                (SPSEQSignature) pp.getSpsEq().sign(
                        vectorToMessageBlock(C),
                        providerKeyPair.getSk().getSkSpsEq()
                )
        );

        assertTrue(pp.getSpsEq().verify(vectorToMessageBlock(token.getC()), token.getSignature(), providerKeyPair.getPk().getPkSpsEq()));

        var earnAmount = 7;
        var earnRequest = incentiveSystem.generateEarnRequest(token, userKeyPair, providerKeyPair.getPk(), s);

        var signature = incentiveSystem.generateEarnRequestResponse(earnRequest, earnAmount, providerKeyPair);
        var newToken = incentiveSystem.handleEarnRequestResponse(earnRequest, signature, earnAmount, token, userKeyPair, providerKeyPair.getPk(), s);

        assertEquals(
                newToken.getPoints().getInteger().longValue(),
                token.getPoints().getInteger().longValue() + earnAmount
        );
        assertTrue(pp.getSpsEq().verify(vectorToMessageBlock(newToken.getC()), newToken.getSignature(), providerKeyPair.getPk().getPkSpsEq()));
    }
}