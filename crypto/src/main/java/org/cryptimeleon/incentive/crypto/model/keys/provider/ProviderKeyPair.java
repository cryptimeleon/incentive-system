package org.cryptimeleon.incentive.crypto.model.keys.provider;

import java.util.Objects;

public class ProviderKeyPair {
    private final ProviderSecretKey sk;
    private final ProviderPublicKey pk;

    public ProviderKeyPair(ProviderSecretKey sk, ProviderPublicKey pk) {
        this.sk = sk;
        this.pk = pk;
    }

    public ProviderSecretKey getSk() {
        return this.sk;
    }

    public ProviderPublicKey getPk() {
        return this.pk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProviderKeyPair that = (ProviderKeyPair) o;
        return Objects.equals(sk, that.sk) && Objects.equals(pk, that.pk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sk, pk);
    }

    public String toString() {
        return "ProviderKeyPair(sk=" + this.getSk() + ", pk=" + this.getPk() + ")";
    }
}
