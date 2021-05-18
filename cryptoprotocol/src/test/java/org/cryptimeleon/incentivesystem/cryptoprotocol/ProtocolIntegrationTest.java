package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.EarnRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.SpendRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.SpendResponse;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinResponse;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.CommitmentWellformednessProtocol;
import org.cryptimeleon.incentivesystem.cryptoprotocol.proof.SpendDeductZkp;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

/**
 * Performs a full example run of all three protocols as in a real-world setting.
 * A new user joins the system using the Issue-Join protocol,
 * then earns some points using the Credit-Earn protocol
 * and performs some (valid as well as invalid) Spend operations.
 * Note: since no state is stored on either side at cryptoprotocol level, it makes no sense to test with multiple users here.
 */
public class ProtocolIntegrationTest
{
    @Test
    public void fullProtocolTestRun()
    {
        System.out.println("Starting integration test of all three cryptographic protocols.");

        /**
         * incentive system setup and user+provider key generation
         */

        System.out.println("Setting up the incentive system and generating keys.");

        // generate incentive system pp and extracts used Zn for shorter references
        var incSys = new IncentiveSystem(IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug));
        var usedZn = incSys.getPp().getBg().getZn();

        // generate provider keys
        var pkp = incSys.generateProviderKeys();

        // generate user key pair for user
        var ukp = incSys.generateUserKeys();

        /**
         * user joins system using issue-join protocol
         */

        System.out.println("A new user joins the system.");

        // user generates join request
        var joinRequest = incSys.generateJoinRequest(
                incSys.getPp(),
                pkp.getPk(),
                ukp
        );

        // serialize and deserialize join request to ensure serialization does not break anything
        var serializedJoinRequest = joinRequest.getRepresentation();
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(incSys.getPp(), pkp.getPk()));
        var deserializedJoinRequest = new JoinRequest(serializedJoinRequest, incSys.getPp(), ukp.getPk(), cwfProofSystem);

        // provider handles join request and generates join response
        var joinResponse = incSys.generateJoinRequestResponse(incSys.getPp(), pkp, ukp.getPk().getUpk(), deserializedJoinRequest);

        // serialize and deserialize join response
        var serializedJoinResponse = joinResponse.getRepresentation();
        var deserializedJoinResponse = new JoinResponse(serializedJoinResponse, incSys.getPp());

        // user handles join response
        var initialToken = incSys.handleJoinRequestResponse(incSys.getPp(), pkp.getPk(), ukp, joinRequest, deserializedJoinResponse);

        /**
         * transaction 1: user tries to spend points with an empty token
         */

        System.out.println("Testing spend transaction with empty token.");

        // generate a fresh ID for the spend transaction
        var tid1 = usedZn.getUniformlyRandomElement();

        var spendAmount1 = BigInteger.ONE;

        // user tries to generate spend request
        try
        {
            var spendRequest1 = incSys.generateSpendRequest(initialToken, pkp.getPk(), spendAmount1, ukp, tid1);
        }
        catch(IllegalArgumentException e)
        {
            System.out.println("Expected exception when trying to spend " + spendAmount1.toString() +" point using token with " + initialToken.getPoints().toString() + " points: " + e.getMessage());
        }

        // ensure user token still contains 0 points
        Assertions.assertEquals(initialToken.getPoints(), usedZn.getZeroElement());

        /**
         * transaction 2: user earns 20 points
         */

        System.out.println("Initialize Credit-Earn execution which grants user 20 points.");

        // user generates earn request
        var earnRequest1 = incSys.generateEarnRequest(initialToken, pkp.getPk(), ukp);

        // serialize and deserialize earn request to ensure serialization does not break anything
        var serializedEarnRequest1 = earnRequest1.getRepresentation();
        var deserializedEarnRequest1 = new EarnRequest(serializedEarnRequest1, incSys.getPp());

        // provider handles earn request and generates earn response
        var earnResponse1 = incSys.generateEarnRequestResponse(deserializedEarnRequest1, new BigInteger("20"), pkp);

        // serialize and deserialize earn response to ensure serialization does not break anything
        var serializedEarnResponse1 = earnResponse1.getRepresentation();
        var deserializedEarnResponse1 = new SPSEQSignature(serializedEarnResponse1, incSys.getPp().getBg().getG1(), incSys.getPp().getBg().getG2());

        // user handles earn response
        var updatedToken = incSys.handleEarnRequestResponse(deserializedEarnRequest1, deserializedEarnResponse1, new BigInteger("20"), initialToken, pkp.getPk(), ukp);

        // ensure user token contains 20 points
        Assertions.assertEquals(updatedToken.getPoints().getInteger(), new BigInteger(("20")));

        /**
         * transaction 3: user tries to spend 23 points
         */

        System.out.println("Testing failing spend transaction with non-empty token.");

        // generate a fresh ID for the spend transaction
        var tid2 = usedZn.getUniformlyRandomElement();

        // define spend amount
        var spendAmount2 = new BigInteger("23");

        // user tries to generate spend request
        try
        {
            var spendRequest2 = incSys.generateSpendRequest(updatedToken, pkp.getPk(), spendAmount2, ukp, tid2);
        }
        catch(IllegalArgumentException e)
        {
            System.out.println("Expected exception when trying to spend " + spendAmount2.toString() +" point using token with " + updatedToken.getPoints().toString() + " points: " + e.getMessage());
        }

        // ensure user token still contains 20 points
        Assertions.assertEquals(updatedToken.getPoints().getInteger(), new BigInteger(("20")));

        /**
         * transaction 4: user spends 18 points
         */

        System.out.println("Testing successful spend transaction.");

        // generate a fresh ID for the spend transaction
        var tid3 = usedZn.getUniformlyRandomElement();

        // define spend amount
        var spendAmount3 = new BigInteger("18");

        // user generates spend request
        SpendRequest spendRequest3 = null;
        try
        {
            spendRequest3 = incSys.generateSpendRequest(updatedToken, pkp.getPk(), spendAmount3, ukp, tid3);
        }
        catch(IllegalArgumentException e)
        {
            System.out.println("Unexpected exception when trying to spend " + spendAmount3.toString() +" point using token with " + updatedToken.getPoints().toString() + " points: " + e.getMessage());
        }

        // serialize and deserialize spend request to ensure that serialization does not break anything
        var serializedSpendRequest3 = spendRequest3.getRepresentation();
        FiatShamirProofSystem spendDeductProofSystem = new FiatShamirProofSystem(new SpendDeductZkp(incSys.getPp(), pkp.getPk()));
        var deserializedSpendRequest3 = new SpendRequest(serializedSpendRequest3, incSys.getPp(), spendDeductProofSystem, spendAmount3, tid3);

        // provider handles spend request and generates spend response and information required for double-spending protection (which is discarded on the fly, since not needed in this test)
        var spendResponse3 = incSys.generateSpendRequestResponse(deserializedSpendRequest3, pkp, spendAmount3, tid3).getSpendResponse();

        // serialize and deserialize spend request to ensure that serialization does not break anything
        var serializedSpendResponse3 = spendResponse3.getRepresentation();
        var deserializedSpendResponse3 = new SpendResponse(serializedSpendResponse3, usedZn, incSys.getPp().getSpsEq());

        // user handles spend response
        updatedToken = incSys.handleSpendRequestResponse(deserializedSpendResponse3, deserializedSpendRequest3, updatedToken, spendAmount3, pkp.getPk(), ukp);

        // ensure that point count of token is 2 = 20-18
        Assertions.assertEquals(updatedToken.getPoints().getInteger(), new BigInteger(("2")));

        System.out.println("Done testing protocols.");
    }
}
