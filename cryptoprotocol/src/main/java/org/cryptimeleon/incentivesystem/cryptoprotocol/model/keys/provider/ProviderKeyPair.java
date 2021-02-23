package org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider;

public class ProviderKeyPair {
    private ProviderPublicKey pk;
    private ProviderSecretKey sk;

    public ProviderKeyPair(ProviderSecretKey sk, ProviderPublicKey pk) {
        this.sk = sk;
        this.pk = pk;
    }

    public ProviderPublicKey getPk() {
        return this.pk;
    }

    public ProviderSecretKey getSk() {
        return this.sk;
    }
}
