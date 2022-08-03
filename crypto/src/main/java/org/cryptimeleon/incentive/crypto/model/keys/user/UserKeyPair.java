package org.cryptimeleon.incentive.crypto.model.keys.user;

import lombok.Value;

@Value
public class UserKeyPair {
    UserPublicKey pk;
    UserSecretKey sk;
}
