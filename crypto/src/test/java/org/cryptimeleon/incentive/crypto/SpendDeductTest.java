package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.incentive.crypto.model.SpendResponse;
import org.cryptimeleon.incentive.crypto.proof.SpendDeductZkp;
import org.cryptimeleon.math.structures.cartesian.Vector;
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

        var budget = Vector.of(BigInteger.valueOf(7), BigInteger.valueOf(8));
        var k = Vector.of(BigInteger.valueOf(4), BigInteger.valueOf(8));
        assertEquals(budget.length(), k.length());

        var promotionParameters = incentiveSystem.generatePromotionParameters(budget.length());
        var token = Helper.generateToken(pp, userKeyPair, providerKeyPair, promotionParameters, budget);

        // length numDigits
        Zn.ZnElement tid = zp.getUniformlyRandomElement();

        var spendRequest = incentiveSystem.generateSpendRequest(
                promotionParameters,
                token,
                providerKeyPair.getPk(),
                k,
                userKeyPair,
                tid
        );

        var serializedSpendRequest = spendRequest.getRepresentation();

        var fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductZkp(pp, providerKeyPair.getPk(), promotionParameters));
        var deserializedSpendRequest = new SpendRequest(serializedSpendRequest, pp, fiatShamirProofSystem, k, tid);
        assertEquals(spendRequest, deserializedSpendRequest);

        var proverOutput = incentiveSystem.generateSpendRequestResponse(promotionParameters, deserializedSpendRequest, providerKeyPair, k, tid);
        var serializedSpendResponse = proverOutput.getSpendResponse().getRepresentation();
        var doubleSpendingTag = proverOutput.getDstag();

        var newToken = incentiveSystem.handleSpendRequestResponse(promotionParameters,
                new SpendResponse(serializedSpendResponse, zp, pp.getSpsEq()),
                spendRequest,
                token,
                k,
                providerKeyPair.getPk(),
                userKeyPair);

        for (int i = 0; i < promotionParameters.getStoreSize(); i++) {
            assertEquals(newToken.getPoints().get(i).asInteger(), budget.get(i).subtract(k.get(i)));
        }
    }
}
