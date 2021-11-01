package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.incentive.crypto.model.SpendResponse;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendHelper;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkp;
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

        Vector<BigInteger> budget = Vector.of(BigInteger.valueOf(7), BigInteger.valueOf(8));
        var newPointsVector = Vector.of(BigInteger.valueOf(4), BigInteger.valueOf(8));
        Vector<BigInteger> spendAmount = Vector.generatePlain(
                i -> budget.get(i).subtract(newPointsVector.get(i)),
                budget.length()
        );

        assertEquals(budget.length(), newPointsVector.length());

        var promotionParameters = incentiveSystem.generatePromotionParameters(budget.length());
        Vector<BigInteger> ignore = Util.getNullBigIntegerVector(budget.length());
        Vector<BigInteger> ones = Util.getNullBigIntegerVector(budget.length());

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

        Vector<BigInteger> negatedSpendAmount = Vector.fromStreamPlain(spendAmount.stream().map(BigInteger::negate));
        var fiatShamirProofSystem = new FiatShamirProofSystem(
                new SpendDeductZkp(
                        new TokenPointsLeaf("TokenPointsLeaf", spendAmount, ignore),
                        new TokenUpdateLeaf("TokenUpdateLeaf", spendAmount, ignore, ones, negatedSpendAmount),
                        pp,
                        promotionParameters,
                        providerKeyPair.getPk()
                )
        );
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
