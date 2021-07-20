package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.incentive.crypto.model.messages.JoinRequest;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.incentive.crypto.model.proofs.CommitmentWellformednessProtocol;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.logging.Logger;

/**
 * contains tests of the crypto behind the issue  {@literal <}-{@literal >} join protocol
 */
public class IssueJoinCryptoTest
{
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * generates a join request, responds to it and handles the response (i.e. a full run of the Issue-Join protocol).
     * Test case simulating a correct use of the protocol.
     * Also tests representations of used requests and responses on the fly.
     */
    @Test
    void fullCorrectTestRun()
    {
        logger.info("Starting unit test of the Issue-Join protocol.");

        logger.info("Setting up the incentive system and generating keys.");

        // create incentive system instance with fresh pp
        var incSys = new IncentiveSystem(IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug), null);

        // generate a provider key pair
        var pkp = incSys.generateProviderKeys();

        // generate a user key pair
        var ukp = incSys.generateUserKeys();

        logger.info("Generating a join request.");

        // create join request
        var testRequest = incSys.generateJoinRequest(
            incSys.getPp(),
            pkp.getPk(),
            ukp
        );

        logger.info("Testing join request serialization.");

        // serialize and deserialize join request to test serialization
        var serializedRequest = testRequest.getRepresentation();
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(incSys.getPp(), pkp.getPk()));
        var deserializedRequest = new JoinRequest(serializedRequest, incSys.getPp(), ukp.getPk(), cwfProofSystem);

        logger.info("Generating a join response.");

        // pass join request to issue logic, generate join response
        var testResponse = incSys.generateJoinRequestResponse(incSys.getPp(), pkp, ukp.getPk().getUpk(), deserializedRequest);

        logger.info("Testing join response serialization.");

        // serialize and deserialize join response
        var serializedResponse = testResponse.getRepresentation();
        var deserializedResponse = new JoinResponse(serializedResponse, incSys.getPp());

        logger.info("Handling join request and updating token.");

        // pass join response to second part of join logic, generate join output
        var testOutput = incSys.handleJoinRequestResponse(incSys.getPp(), pkp.getPk(), ukp, testRequest, deserializedResponse);

        logger.info("Checking integrity of token.");

        // check output token for sanity (certficate valid, zero points)
        SPSEQSignatureScheme usedSpsEq = incSys.getPp().getSpsEq();
        Assertions.assertTrue(usedSpsEq.verify(pkp.getPk().getPkSpsEq(), testOutput.getSignature(), testOutput.getCommitment0(), testOutput.getCommitment1()));
        Assertions.assertTrue(testOutput.getPoints().asInteger().compareTo(BigInteger.ZERO) == 0);
    }
}
