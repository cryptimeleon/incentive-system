package org.cryptimeleon.incentive.client.integrationtest;

import lombok.Value;
import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.math.serialization.converter.JSONConverter;


/**
 * Some utility workflows that can be re-used.
 */
public class TestHelper {

    private static final JSONConverter jsonConverter = new JSONConverter();

    static TestCryptoAssets getCryptoAssets(InfoClient infoClient, String providerSharedSecret) {
        IncentivePublicParameters pp = new IncentivePublicParameters(jsonConverter.deserialize(infoClient.querySerializedPublicParameters().block()));
        ProviderSecretKey providerSecretKey = new ProviderSecretKey(jsonConverter.deserialize(infoClient.querySerializedProviderSecretKey(providerSharedSecret).block()), pp);
        ProviderPublicKey providerPublicKey = new ProviderPublicKey(jsonConverter.deserialize(infoClient.querySerializedProviderPublicKey().block()), pp);
        ProviderKeyPair providerKeyPair = new ProviderKeyPair(providerSecretKey, providerPublicKey);
        UserKeyPair userKeyPair = (new IncentiveSystem(pp)).generateUserKeys();
        return new TestCryptoAssets(pp, providerKeyPair, userKeyPair);
    }
}

@Value
class TestCryptoAssets {
    IncentivePublicParameters publicParameters;
    ProviderKeyPair providerKeyPair;
    UserKeyPair userKeyPair;
}