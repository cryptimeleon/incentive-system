package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.TestSuite;
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
        var promotionParameters = IncentiveSystem.generatePromotionParameters(2);

        logger.info("Testing represention of tokens");
        var token = Helper.generateToken(TestSuite.pp, TestSuite.userKeyPair, TestSuite.providerKeyPair, promotionParameters);
        var deserializedToken = new Token(token.getRepresentation(), TestSuite.pp);
        assertEquals(deserializedToken, token);
    }
}
