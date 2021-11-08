package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*
 * Contains test cases for the Public Parameters.
 */
public class PublicParameterTest {

    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    void testSetup() {
        logger.info("Testing representation of Public Parameters");
        var pp = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug);
        var deserializedPP = new IncentivePublicParameters(pp.getRepresentation());

        System.out.println(pp);
        System.out.println(deserializedPP);
        assertEquals(deserializedPP, pp);
    }
}
