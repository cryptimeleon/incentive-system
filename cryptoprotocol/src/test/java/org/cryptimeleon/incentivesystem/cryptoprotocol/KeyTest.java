package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserSecretKey;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * This class contains tests of the key representations.
 */
public class KeyTest {

    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    void testProviderKeyPair() {
        var pp = IncentiveSystem.setup();
        var incentiveSystem = new IncentiveSystem(pp);
        var providerKeyPair = incentiveSystem.generateProviderKeys();

        logger.info("Provider Secret Key representation");
        var providerSecretKey = providerKeyPair.getSk();
        var deserializedProviderSecretKey = new ProviderSecretKey(providerSecretKey.getRepresentation(), pp.getSpsEq(), pp.getBg().getZn(), pp.getPrfToZn());
        assertEquals(deserializedProviderSecretKey, providerSecretKey);

        logger.info("Provider Public Key representation");
        var providerPublicKey = providerKeyPair.getPk();
        var deserializedProviderPublicKey = new ProviderPublicKey(providerPublicKey.getRepresentation(), pp.getSpsEq(), pp.getBg().getG1());
        assertEquals(deserializedProviderPublicKey, providerPublicKey);
    }

    @Test
    void testUserKeyPair() {
        var pp = IncentiveSystem.setup();
        var incentiveSystem = new IncentiveSystem(pp);
        var userKeyPair = incentiveSystem.generateUserKeys();

        logger.info("User Secret Key representation");
        var userSecretKey = userKeyPair.getSk();
        var deserializedUserSecretKey = new UserSecretKey(userSecretKey.getRepresentation(), pp.getBg().getZn(), pp.getPrfToZn());
        assertEquals(deserializedUserSecretKey, userSecretKey);

        logger.info("User Public Key representation");
        var userPublicKey = userKeyPair.getPk();
        var deserializedUserPublicKey = new UserPublicKey(userPublicKey.getRepresentation(), pp.getBg().getG1());
        assertEquals(deserializedUserPublicKey, userPublicKey);
    }
}
