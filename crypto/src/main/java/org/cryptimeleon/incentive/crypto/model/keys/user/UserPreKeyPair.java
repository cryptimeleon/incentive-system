package org.cryptimeleon.incentive.crypto.model.keys.user;

import lombok.Value;

@Value
public class UserPreKeyPair {
    UserPublicKey pk;
    UserPreSecretKey psk;
}
