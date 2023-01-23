package org.cryptimeleon.incentive.crypto.model.keys.store;

import org.cryptimeleon.craco.sig.ecdsa.ECDSAVerificationKey;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

import java.util.Objects;

public class StorePublicKey implements StandaloneRepresentable {

    @Represented
    private ECDSAVerificationKey ecdsaVerificationKey;

    public StorePublicKey(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    public StorePublicKey(ECDSAVerificationKey ecdsaVerificationKey) {
        this.ecdsaVerificationKey = ecdsaVerificationKey;
    }

    public ECDSAVerificationKey getEcdsaVerificationKey() {
        return ecdsaVerificationKey;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorePublicKey that = (StorePublicKey) o;
        return Objects.equals(ecdsaVerificationKey, that.ecdsaVerificationKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ecdsaVerificationKey);
    }
}
