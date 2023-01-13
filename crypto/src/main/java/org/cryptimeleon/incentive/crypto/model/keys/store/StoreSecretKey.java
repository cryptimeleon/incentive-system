package org.cryptimeleon.incentive.crypto.model.keys.store;

import org.cryptimeleon.craco.sig.ecdsa.ECDSASigningKey;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

import java.util.Objects;


public class StoreSecretKey implements Representable {
    @Represented
    private ECDSASigningKey ecdsaSigningKey;

    public StoreSecretKey(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    public StoreSecretKey(ECDSASigningKey ecdsaSigningKey) {
        this.ecdsaSigningKey = ecdsaSigningKey;
    }

    public ECDSASigningKey getEcdsaSigningKey() {
        return ecdsaSigningKey;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreSecretKey that = (StoreSecretKey) o;
        return Objects.equals(ecdsaSigningKey, that.ecdsaSigningKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ecdsaSigningKey);
    }
}
