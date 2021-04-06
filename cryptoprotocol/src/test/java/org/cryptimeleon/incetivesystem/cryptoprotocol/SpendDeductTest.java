package org.cryptimeleon.incetivesystem.cryptoprotocol;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.SpendRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.proof.SpendDeductZkp;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpendDeductTest {

    @Test
    void firstTest() {
        var pp = IncentiveSystem.setup();
        var incentiveSystem = new IncentiveSystem(pp);
        var providerKeyPair = incentiveSystem.generateProviderKeys();
        var userKeyPair = incentiveSystem.generateUserKeys();
        var zp = pp.getBg().getZn();

        BigInteger budget = BigInteger.valueOf(7);
        BigInteger k = BigInteger.valueOf(4);
        var token = Helper.generateToken(pp, userKeyPair, providerKeyPair, budget);

        // TODO retrieve these using PRF?
        Zn.ZnElement eskUsrS = zp.getUniformlyRandomElement();
        Zn.ZnElement dsrnd0S = zp.getUniformlyRandomElement();
        Zn.ZnElement dsrnd1S = zp.getUniformlyRandomElement();
        Zn.ZnElement zS = zp.getUniformlyRandomElement();
        Zn.ZnElement tS = zp.getUniformlyRandomElement();
        Zn.ZnElement uS = zp.getUniformlyRandomElement();
        Vector<Zn.ZnElement> vectorR = Vector.generatePlain(
                zp::getUniformlyRandomElement,
                pp.getNumEskDigits()
        );

        // length numDigits
        Zn.ZnElement tid = zp.getUniformlyRandomElement();
        // TODO how is this retrieved in practise?

        var spendRequest = incentiveSystem.generateSpendRequest(
                token,
                providerKeyPair.getPk(),
                k,
                userKeyPair,
                eskUsrS,
                dsrnd0S,
                dsrnd1S,
                zS,
                tS,
                uS,
                vectorR,
                tid
        );

        var serializedSpendRequest = spendRequest.getRepresentation();

        var fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductZkp(pp, providerKeyPair.getPk()));
        var deserializedSpendRequest = new SpendRequest(serializedSpendRequest, pp, fiatShamirProofSystem, k, tid);
        assertEquals(spendRequest, deserializedSpendRequest);

        var proverOutput = incentiveSystem.generateSpendRequestResponse(deserializedSpendRequest, providerKeyPair, k, tid);
        var spendResponse = proverOutput.getSpendResponse();
        var doubleSpendingTag = proverOutput.getDstag();

        var newToken = incentiveSystem.handleSpendRequestResponse(spendResponse, spendRequest, token, providerKeyPair.getPk(), k, userKeyPair, eskUsrS, dsrnd0S, dsrnd1S, zS, tS, uS, vectorR, tid);
    }
}
