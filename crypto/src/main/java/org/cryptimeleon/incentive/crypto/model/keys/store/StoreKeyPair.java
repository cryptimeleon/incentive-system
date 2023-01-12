package org.cryptimeleon.incentive.crypto.model.keys.store;

import java.util.Objects;

public class StoreKeyPair {
    private final StoreSecretKey sk;
    private final StorePublicKey pk;

    public StoreKeyPair(StoreSecretKey sk, StorePublicKey pk) {
        this.sk = sk;
        this.pk = pk;
    }

    public StoreSecretKey getSk() {
        return this.sk;
    }

    public StorePublicKey getPk() {
        return this.pk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreKeyPair that = (StoreKeyPair) o;
        return Objects.equals(sk, that.sk) && Objects.equals(pk, that.pk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sk, pk);
    }

    public String toString() {
        return "StoreKeyPair(sk=" + this.getSk() + ", pk=" + this.getPk() + ")";
    }
}
