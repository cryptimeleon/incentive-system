package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.incentive.crypto.model.SpendResponse;
import org.cryptimeleon.incentive.crypto.proof.spend.MetadataZkp;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendHelper;
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
        var newPointsVector = Vector.of(BigInteger.valueOf(4), BigInteger.valueOf(8));
        var spendAmount = new BigInteger[budget.length()];
        for (int i = 0; i < budget.length(); i++) {
            spendAmount[i] = budget.get(i).subtract(newPointsVector.get(i));
        }

        assertEquals(budget.length(), newPointsVector.length());

        var promotionParameters = incentiveSystem.generatePromotionParameters(budget.length());
        var token = Helper.generateToken(pp, userKeyPair, providerKeyPair, promotionParameters, budget);
        var spendDeductTestZkp = SpendHelper.generateSimpleTestSpendDeductZkp(pp, promotionParameters, providerKeyPair.getPk(), spendAmount);

        // length numDigits
        Zn.ZnElement tid = zp.getUniformlyRandomElement();

        var spendRequest = incentiveSystem.generateSpendRequest(
                promotionParameters,
                token,
                providerKeyPair.getPk(),
                newPointsVector,
                userKeyPair,
                tid,
                spendDeductTestZkp
        );

        var serializedSpendRequest = spendRequest.getRepresentation();

        var fiatShamirProofSystem = new FiatShamirProofSystem(new MetadataZkp(pp, providerKeyPair.getPk(), promotionParameters));
        var deserializedSpendRequest = new SpendRequest(serializedSpendRequest, pp, fiatShamirProofSystem, tid);
        assertEquals(spendRequest, deserializedSpendRequest);

        var proverOutput = incentiveSystem.generateSpendRequestResponse(promotionParameters,
                deserializedSpendRequest,
                providerKeyPair,
                tid,
                spendDeductTestZkp);
        var serializedSpendResponse = proverOutput.getSpendResponse().getRepresentation();
        var doubleSpendingTag = proverOutput.getDstag();

        var newToken = incentiveSystem.handleSpendRequestResponse(promotionParameters,
                new SpendResponse(serializedSpendResponse, zp, pp.getSpsEq()),
                spendRequest,
                token,
                newPointsVector,
                providerKeyPair.getPk(),
                userKeyPair);

        for (int i = 0; i < promotionParameters.getPointsVectorSize(); i++) {
            assertEquals(newToken.getPoints().get(i).asInteger(), newPointsVector.get(i));
        }
    }
}
