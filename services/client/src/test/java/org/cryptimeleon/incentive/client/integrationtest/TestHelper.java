package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.store.StoreKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.store.StorePublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.store.StoreSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPreKeyPair;
import org.cryptimeleon.math.serialization.converter.JSONConverter;

import java.util.Objects;

/**
 * Some utility workflows that can be re-used.
 */
public class TestHelper {
    private static final JSONConverter jsonConverter = new JSONConverter();

    static TestCryptoAssets getCryptoAssets(InfoClient infoClient, String providerSharedSecret, String storeSharedSecret) {
        IncentivePublicParameters pp = new IncentivePublicParameters(jsonConverter.deserialize(infoClient.querySerializedPublicParameters().block()));
        ProviderSecretKey providerSecretKey = new ProviderSecretKey(jsonConverter.deserialize(infoClient.querySerializedProviderSecretKey(providerSharedSecret).block()), pp);
        ProviderPublicKey providerPublicKey = new ProviderPublicKey(jsonConverter.deserialize(infoClient.querySerializedProviderPublicKey().block()), pp);
        StorePublicKey storePublicKey = new StorePublicKey(jsonConverter.deserialize(infoClient.querySerializedStorePublicKey().block()));
        StoreSecretKey storeSecretKey = new StoreSecretKey(jsonConverter.deserialize(infoClient.querySerializedStoreSecretKey(storeSharedSecret).block()));
        ProviderKeyPair providerKeyPair = new ProviderKeyPair(providerSecretKey, providerPublicKey);
        StoreKeyPair storeKeyPair = new StoreKeyPair(storeSecretKey, storePublicKey);
        UserPreKeyPair userPreKeyPair = (new IncentiveSystem(pp)).generateUserPreKeyPair();
        UserKeyPair userKeyPair = Util.addRegistrationSignatureToUserPreKeys(userPreKeyPair, providerKeyPair, pp);
        return new TestCryptoAssets(pp, providerKeyPair, storeKeyPair, userKeyPair);
    }
}

final class TestCryptoAssets {
    private final IncentivePublicParameters publicParameters;
    private final ProviderKeyPair providerKeyPair;
    private final StoreKeyPair storeKeyPair;
    private final UserKeyPair userKeyPair;

    public TestCryptoAssets(IncentivePublicParameters publicParameters, ProviderKeyPair providerKeyPair, StoreKeyPair storeKeyPair, UserKeyPair userKeyPair) {
        this.publicParameters = publicParameters;
        this.providerKeyPair = providerKeyPair;
        this.storeKeyPair = storeKeyPair;
        this.userKeyPair = userKeyPair;
    }

    public IncentivePublicParameters getPublicParameters() {
        return this.publicParameters;
    }

    public ProviderKeyPair getProviderKeyPair() {
        return this.providerKeyPair;
    }

    public UserKeyPair getUserKeyPair() {
        return this.userKeyPair;
    }

    public StoreKeyPair getStoreKeyPair() {
        return storeKeyPair;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCryptoAssets that = (TestCryptoAssets) o;
        return Objects.equals(publicParameters, that.publicParameters) && Objects.equals(providerKeyPair, that.providerKeyPair) && Objects.equals(storeKeyPair, that.storeKeyPair) && Objects.equals(userKeyPair, that.userKeyPair);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicParameters, providerKeyPair, storeKeyPair, userKeyPair);
    }
}
