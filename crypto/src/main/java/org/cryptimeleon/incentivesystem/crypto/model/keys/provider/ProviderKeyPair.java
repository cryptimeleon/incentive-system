package org.cryptimeleon.incentivesystem.crypto.model.keys.provider;

import lombok.Value;
import org.cryptimeleon.math.serialization.annotations.Represented;

@Value
public class ProviderKeyPair {
    @Represented
    ProviderSecretKey sk;

    @Represented
    ProviderPublicKey pk;
}
