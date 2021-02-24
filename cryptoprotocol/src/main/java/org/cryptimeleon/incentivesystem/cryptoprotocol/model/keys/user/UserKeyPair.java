package org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user;

import lombok.Value;
import org.cryptimeleon.math.serialization.annotations.Represented;

@Value
public class UserKeyPair {
    @Represented
    UserPublicKey pk;

    @Represented
    UserSecretKey sk;
}
