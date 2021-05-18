package org.cryptimeleon.incentivesystem.cryptoprotocol;

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
        /**
         * incentive system setup and user+provider key generation
         */

        // generate incentive system pp
        var incSys = new IncentiveSystem(IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug));

        // generate provider keys
        var pkp = incSys.generateProviderKeys();

        // generate user key pair for user
        var ukp = incSys.generateUserKeys();

        /**
         * user joins system using issue-join protocol
         */

        // user generates join request
        var joinRequest = incSys.generateJoinRequest(
                incSys.getPp(),
                pkp.getPk(),
                ukp
        );

        // provider handles join request and generates join response
        var joinResponse = incSys.generateJoinRequestResponse(incSys.getPp(), pkp, ukp.getPk().getUpk(), joinRequest);

        // user handles join response
        var initialToken = incSys.handleJoinRequestResponse(incSys.getPp(), pkp.getPk(), ukp, joinRequest, joinResponse);

        /**
         * transaction 1: user tries to spend points with an empty token
         */

        // generate a fresh ID for the spend transaction
        var usedZn = incSys.getPp().getBg().getZn();
        var tid1 = usedZn.getUniformlyRandomElement();

        // user generates spend request
        var spendRequest = incSys.generateSpendRequest(initialToken, pkp.getPk(), BigInteger.ONE, ukp, tid1);

        // provider handles spend request but does not generate spend response
        var spendResponse = incSys.generateSpendRequestResponse(spendRequest, pkp, BigInteger.ONE, tid1);

        // ensure user token still contains 0 points
        Assertions.assertEquals(initialToken.getPoints(), usedZn.getZeroElement());

        /**
         * transaction 2: user earns 20 points
         */

        // user generates earn request

        // provider handles earn request and generates earn response

        // user handles earn response

        // ensure user token contains 20 points

        /**
         * transaction 3: user tries to spend 23 points
         */

        // user generates spend request

        // provider handles spend request but does not generate spend response

        // ensure user token still contains 20 points

        /**
         * transaction 4: user spends 18 points
         */

        // user generates spend request

        // provider handles spend request and generates spend response

        // user handles spend response

        // ensure that point count of token is 2 = 20-18
    }
}
