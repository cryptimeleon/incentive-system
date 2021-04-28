package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.SpendRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.SpendResponse;
import org.cryptimeleon.incentivesystem.cryptoprotocol.proof.SpendDeductZkp;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpendDeductTest {

    @Test
    void testSuccessFullSpendDeduct() {
        var pp = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug);
        var incentiveSystem = new IncentiveSystem(pp);
        var providerKeyPair = incentiveSystem.generateProviderKeys();
        var userKeyPair = incentiveSystem.generateUserKeys();
        var zp = pp.getBg().getZn();

        BigInteger budget = BigInteger.valueOf(7);
        BigInteger k = BigInteger.valueOf(4);
        var token = Helper.generateToken(pp, userKeyPair, providerKeyPair, budget);

        // length numDigits
        Zn.ZnElement tid = zp.getUniformlyRandomElement();

        var spendRequest = incentiveSystem.generateSpendRequest(
                token,
                providerKeyPair.getPk(),
                k,
                userKeyPair,
                tid
        );

        var serializedSpendRequest = spendRequest.getRepresentation();

        var fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductZkp(pp, providerKeyPair.getPk()));
        var deserializedSpendRequest = new SpendRequest(serializedSpendRequest, pp, fiatShamirProofSystem, k, tid);
        assertEquals(spendRequest, deserializedSpendRequest);

        var proverOutput = incentiveSystem.generateSpendRequestResponse(deserializedSpendRequest, providerKeyPair, k, tid);
        var serializedSpendResponse = proverOutput.getSpendResponse().getRepresentation();
        var doubleSpendingTag = proverOutput.getDstag();

        var newToken = incentiveSystem.handleSpendRequestResponse(new SpendResponse(serializedSpendResponse, zp, pp.getSpsEq()), spendRequest, token, k, providerKeyPair.getPk(), userKeyPair);
        assertEquals(newToken.getPoints().getInteger(), budget.subtract(k));
    }
}
