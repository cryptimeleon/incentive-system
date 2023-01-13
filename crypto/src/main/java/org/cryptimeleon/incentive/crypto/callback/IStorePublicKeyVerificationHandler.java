package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.incentive.crypto.model.keys.store.StorePublicKey;

/**
 * Interface to support a lambda function as a callback to verify the store public key.
 */
public interface IStorePublicKeyVerificationHandler {
    boolean isStorePublicKeyTrusted(StorePublicKey storePublicKey);
}
