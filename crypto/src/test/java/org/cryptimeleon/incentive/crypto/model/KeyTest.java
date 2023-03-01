package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.store.StorePublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.store.StoreSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPreSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * This class contains tests of the key representations.
 */
public class KeyTest {

    final IncentivePublicParameters pp = TestSuite.pp;
    final IncentiveSystem incentiveSystem = TestSuite.incentiveSystem;
    final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    void testStoreKeyPair() {
        var storeKeyPair = incentiveSystem.generateStoreKeyPair();

        logger.info("Store Secret Key representation");
        var storeSecretKey = storeKeyPair.getSk();
        var deserializedStoreSecretKey = new StoreSecretKey(storeSecretKey.getRepresentation());
        assertEquals(storeSecretKey, deserializedStoreSecretKey);

        logger.info("Store Public Key representation");
        var storePublicKey = storeKeyPair.getPk();
        var deserializedStorePublicKey = new StorePublicKey(storePublicKey.getRepresentation());
        assertEquals(storePublicKey, deserializedStorePublicKey);
    }

    @Test
    void testProviderKeyPair() {
        var providerKeyPair = incentiveSystem.generateProviderKeyPair();

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
        var userKeyPair = TestSuite.userKeyPair;

        logger.info("User Secret Key representation");
        var userSecretKey = userKeyPair.getSk();
        var deserializedUserSecretKey = new UserSecretKey(userSecretKey.getRepresentation(), pp);
        assertEquals(deserializedUserSecretKey, userSecretKey);

        logger.info("User Public Key representation");
        var userPublicKey = userKeyPair.getPk();
        var deserializedUserPublicKey = new UserPublicKey(userPublicKey.getRepresentation(), pp);
        assertEquals(deserializedUserPublicKey, userPublicKey);
    }

    @Test
    void testUserPreKeyPair() {
        var userKeyPair = TestSuite.userPreKeyPair;

        logger.info("User Secret Key representation");
        var userSecretKey = userKeyPair.getPsk();
        var deserializedUserSecretKey = new UserPreSecretKey(userSecretKey.getRepresentation(), pp);
        assertEquals(deserializedUserSecretKey, userSecretKey);

        logger.info("User Public Key representation");
        var userPublicKey = userKeyPair.getPk();
        var deserializedUserPublicKey = new UserPublicKey(userPublicKey.getRepresentation(), pp);
        assertEquals(deserializedUserPublicKey, userPublicKey);
    }
}
