package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.incentive.crypto.model.messages.JoinRequest;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessProtocol;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

/**
 * contains tests of the crypto behind the issue  {@literal <}-{@literal >} join protocol
 */
public class IssueJoinCryptoTest {
    /**
     * generates a join request, responds to it and handles the response (i.e. a full run of the Issue-Join protocol).
     * Test case simulating a correct use of the protocol.
     * Also tests representations of used requests and responses on the fly.
     */
    @Test
    void fullCorrectTestRun() {
        // create incentive system instance with fresh pp
        var incSys = new IncentiveSystem(IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug));

        // generate a provider key pair
        var pkp = incSys.generateProviderKeys();

        // generate a user key pair
        var ukp = incSys.generateUserKeys();

        // generate promotion parameters
        var promotionParameters = incSys.generatePromotionParameters(5);

        // create join request
        var testRequest = incSys.generateJoinRequest(
                pkp.getPk(),
                ukp
        );

        // serialize and deserialize join request to test serialization
        var serializedRequest = testRequest.getRepresentation();
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(incSys.getPp(), pkp.getPk()));
        var deserializedRequest = new JoinRequest(serializedRequest, incSys.getPp(), ukp.getPk(), cwfProofSystem);

        // pass join request to issue logic, generate join response
        var testResponse = incSys.generateJoinRequestResponse(promotionParameters, pkp, ukp.getPk().getUpk(), deserializedRequest);

        // serialize and deserialize join response
        var serializedResponse = testResponse.getRepresentation();
        var deserializedResponse = new JoinResponse(serializedResponse, incSys.getPp());

        // pass join response to second part of join logic, generate join output
        var testOutput = incSys.handleJoinRequestResponse(promotionParameters, pkp.getPk(), ukp, testRequest, deserializedResponse);

        // check output token for sanity (certficate valid, zero points)
        SPSEQSignatureScheme usedSpsEq = incSys.getPp().getSpsEq();
        Assertions.assertTrue(usedSpsEq.verify(pkp.getPk().getPkSpsEq(),
                testOutput.getSignature(),
                testOutput.getCommitment0(),
                testOutput.getCommitment1(),
                testOutput.getCommitment1().pow(promotionParameters.getPromotionId())));
        Assertions.assertTrue(testOutput.getPoints().stream().allMatch(e -> e.asInteger().compareTo(BigInteger.ZERO) == 0));
    }
}
