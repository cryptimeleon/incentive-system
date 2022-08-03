package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey;
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
        var pp = IncentiveSystem.setup(512, Setup.BilinearGroupChoice.Debug);
        var incentiveSystem = new IncentiveSystem(pp);
        var providerKeyPair = incentiveSystem.generateProviderKeys();

        logger.info("Provider Secret Key representation");
        var providerSecretKey = providerKeyPair.getSk();
        var deserializedProviderSecretKey = new ProviderSecretKey(providerSecretKey.getRepresentation(), pp);
        assertEquals(deserializedProviderSecretKey, providerSecretKey);

        logger.info("Provider Public Key representation");
        var providerPublicKey = providerKeyPair.getPk();
        var deserializedProviderPublicKey = new ProviderPublicKey(providerPublicKey.getRepresentation(), pp);
        assertEquals(deserializedProviderPublicKey, providerPublicKey);
    }

    @Test
    void testUserKeyPair() {
        var pp = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug);
        var incentiveSystem = new IncentiveSystem(pp);
        var userKeyPair = incentiveSystem.generateUserKeys();

        logger.info("User Secret Key representation");
        var userSecretKey = userKeyPair.getSk();
        var deserializedUserSecretKey = new UserSecretKey(userSecretKey.getRepresentation(), pp);
        assertEquals(deserializedUserSecretKey, userSecretKey);

        logger.info("User Public Key representation");
        var userPublicKey = userKeyPair.getPk();
        var deserializedUserPublicKey = new UserPublicKey(userPublicKey.getRepresentation(), pp);
        assertEquals(deserializedUserPublicKey, userPublicKey);
    }
}
