package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*
 * Contains tests of the Token class.
 */
public class TokenTest {

    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    void testTokenSerialization() {
        var pp = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug);
        var incentiveSystem = new IncentiveSystem(pp);
        var providerKeyPair = incentiveSystem.generateProviderKeys();
        var userKeyPair = incentiveSystem.generateUserKeys();
        var promotionParameters = incentiveSystem.generatePromotionParameters(2);

        logger.info("Testing represention of tokens");
        var token = Helper.generateToken(pp, userKeyPair, providerKeyPair, promotionParameters);
        var deserializedToken = new Token(token.getRepresentation(), pp);
        assertEquals(deserializedToken, token);
    }
}
