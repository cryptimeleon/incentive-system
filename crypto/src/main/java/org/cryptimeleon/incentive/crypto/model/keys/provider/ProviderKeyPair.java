package org.cryptimeleon.incentive.crypto.model.keys.provider;

import lombok.Value;
import org.cryptimeleon.math.serialization.annotations.Represented;

@Value
public class ProviderKeyPair {
    ProviderSecretKey sk;
    ProviderPublicKey pk;
}
