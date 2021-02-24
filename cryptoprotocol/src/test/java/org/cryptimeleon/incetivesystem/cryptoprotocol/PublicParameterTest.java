package org.cryptimeleon.incetivesystem.cryptoprotocol;

import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Contains test cases for the Public Parameters.
 */
public class PublicParameterTest {

    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    void testSetup() {
        logger.info("Testing representation of Public Parameters");
        var pp = IncentiveSystem.setup();
        var deserializedPP = new IncentivePublicParameters(pp.getRepresentation());

        assertThat(deserializedPP).isEqualTo(pp);
    }
}
