package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinResponse;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.CommitmentWellformednessProtocol;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

/**
 * contains tests of the crypto behind the issue <-> join protocol
 */
public class IssueJoinCryptoTest
{
    /**
     * generates a join request, responds to it and handles the response (i.e. a full run of the Issue-Join protocol).
     * Test case simulating a correct use of the protocol.
     * Also tests representations of used requests and responses on the fly.
     */
    @Test
    void fullCorrectTestRun()
    {
        // create incentive system instance with fresh pp
        var incSys = new IncentiveSystem(IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug));

        // generate a provider key pair
        var pkp = incSys.generateProviderKeys();

        // generate a user key pair
        var ukp = incSys.generateUserKeys();

        // generate randomness for join request TODO: the 6 ZnElements shall be local variables of generateJoinRequest, the code below is just a workaround until PRF-based randomness generation has been figured out
        Zn usedZn = incSys.getPp().getBg().getZn();
        Zn.ZnElement eskUsr  = usedZn.getUniformlyRandomElement(); // user's share of the encryption secret key for the tracing information's encryption
        Zn.ZnElement dsrnd0  = usedZn.getUniformlyRandomElement(); // randomness for the first challenge generation in double-spending protection
        Zn.ZnElement dsrnd1  = usedZn.getUniformlyRandomElement(); // randomness for the second challenge generation in double-spending protection
        Zn.ZnElement z  = usedZn.getUniformlyRandomElement(); // blinding randomness needed to make (C^u, g^u) uniformly random
        Zn.ZnElement t  = usedZn.getUniformlyRandomElement(); // blinding randomness needed for special DDH trick in a proof
        Zn.ZnElement u  = usedZn.getUniformlyRandomNonzeroElement(); // != 0 needed for trick in the commitment well-formedness proof

        // create join request
        var testRequest = incSys.generateJoinRequest(
            incSys.getPp(),
            pkp.getPk(),
            ukp
        );

        // serialize and deserialize join request to test serialization
        var serializedRequest = testRequest.getRepresentation();
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(incSys.getPp(), pkp.getPk()));
        var deserializedRequest = new JoinRequest(serializedRequest, incSys.getPp(), ukp.getPk(), cwfProofSystem);

        // pass join request to issue logic, generate join response
        var testResponse = incSys.generateJoinRequestResponse(incSys.getPp(), pkp, ukp.getPk().getUpk(), deserializedRequest);

        // serialize and deserialize join response
        var serializedResponse = testResponse.getRepresentation();
        var deserializedResponse = new JoinResponse(serializedResponse, incSys.getPp());

        // pass join response to second part of join logic, generate join output
        var testOutput = incSys.handleJoinRequestResponse(incSys.getPp(), pkp.getPk(), ukp, testRequest, deserializedResponse);

        // check output token for sanity (certficate valid, zero points)
        SPSEQSignatureScheme usedSpsEq = incSys.getPp().getSpsEq();
        Assertions.assertTrue(usedSpsEq.verify(pkp.getPk().getPkSpsEq(), testOutput.getSignature(), testOutput.getCommitment0(), testOutput.getCommitment1()));
        Assertions.assertTrue(testOutput.getPoints().asInteger().compareTo(BigInteger.ZERO) == 0);
    }
}
