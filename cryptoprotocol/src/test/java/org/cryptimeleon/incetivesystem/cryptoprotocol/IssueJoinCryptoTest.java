package org.cryptimeleon.incetivesystem.cryptoprotocol;

import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinRequest;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.Test;

/**
 * contains tests of the crypto behind the issue <-> join protocol
 */
public class IssueJoinCryptoTest
{
    /**
     * generates a join request, responds to it and handles the response (i.e. a full run of the Issue-Join protocol).
     * Test case simulating a correct use of the protocol.
     */
    @Test
    void fullCorrectTestRun()
    {
        // create incentive system instance with fresh pp
        var incSys = new IncentiveSystem(IncentiveSystem.setup());

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
            ukp,
            eskUsr,
            dsrnd0,
            dsrnd1,
            z,
            t,
            u
        );

        // pass join request to issue logic, generate join response
        var testResponse = incSys.generateJoinRequestResponse(incSys.getPp(), pkp, testRequest);

        // pass join response to second part of join logic, generate join output
        var testOutput = incSys.handleJoinRequestResponse(incSys.getPp(), pkp.getPk(), ukp, testRequest, testResponse, eskUsr, dsrnd0, dsrnd1, z, t, u);
    }

    /**
     * generates a dummy join request, then serializes and deserializes it,
     * testing whether the result of the deserialization is equal to the original request object.
     */
    @Test
    void joinRequestRepresentationTest()
    {

    }

    /**
     * generates a dummy join response, then serializes and deserializes it,
     * testing whether the result of the deserialization is equal to the original response object.
     */
    @Test
    void joinResponseRepresentationTest()
    {

    }

    /**
     * generates a dummy join output, then serializes and deserializes it,
     * testing whether the result of the deserialization is equal to the original output object.
     */
    @Test
    void joinOutputRepresentationTest()
    {

    }
}
