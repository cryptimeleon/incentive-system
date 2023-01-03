package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPreKeyPair;
import org.cryptimeleon.math.serialization.converter.JSONConverter;

import java.util.Objects;

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
        UserPreKeyPair userPreKeyPair = (new IncentiveSystem(pp)).generateUserPreKeyPair();
        UserKeyPair userKeyPair = Util.addGenesisSignatureToUserKeys(userPreKeyPair, providerKeyPair, pp);
        return new TestCryptoAssets(pp, providerKeyPair, userKeyPair);
    }
}

final class TestCryptoAssets {
    private final IncentivePublicParameters publicParameters;
    private final ProviderKeyPair providerKeyPair;
    private final UserKeyPair userKeyPair;

    public TestCryptoAssets(final IncentivePublicParameters publicParameters, final ProviderKeyPair providerKeyPair, final UserKeyPair userKeyPair) {
        this.publicParameters = publicParameters;
        this.providerKeyPair = providerKeyPair;
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

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TestCryptoAssets)) return false;
        final TestCryptoAssets other = (TestCryptoAssets) o;
        final Object this$publicParameters = this.getPublicParameters();
        final Object other$publicParameters = other.getPublicParameters();
        if (!Objects.equals(this$publicParameters, other$publicParameters))
            return false;
        final Object this$providerKeyPair = this.getProviderKeyPair();
        final Object other$providerKeyPair = other.getProviderKeyPair();
        if (!Objects.equals(this$providerKeyPair, other$providerKeyPair))
            return false;
        final Object this$userKeyPair = this.getUserKeyPair();
        final Object other$userKeyPair = other.getUserKeyPair();
        return Objects.equals(this$userKeyPair, other$userKeyPair);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $publicParameters = this.getPublicParameters();
        result = result * PRIME + ($publicParameters == null ? 43 : $publicParameters.hashCode());
        final Object $providerKeyPair = this.getProviderKeyPair();
        result = result * PRIME + ($providerKeyPair == null ? 43 : $providerKeyPair.hashCode());
        final Object $userKeyPair = this.getUserKeyPair();
        result = result * PRIME + ($userKeyPair == null ? 43 : $userKeyPair.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "TestCryptoAssets(publicParameters=" + this.getPublicParameters() + ", providerKeyPair=" + this.getProviderKeyPair() + ", userKeyPair=" + this.getUserKeyPair() + ")";
    }
}
