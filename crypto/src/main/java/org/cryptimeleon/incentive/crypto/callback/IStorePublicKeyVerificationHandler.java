package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.incentive.crypto.model.keys.store.StorePublicKey;

/**
 * Interface to support a lambda function as a callback to verify the store public key.
 */
public interface IStorePublicKeyVerificationHandler {
    /**
     * Returns true if the provided key shall be seen as trusted, i.e. belongs to a store that is part of the incentive
     * system.
     */
    boolean isStorePublicKeyTrusted(StorePublicKey storePublicKey);
}
