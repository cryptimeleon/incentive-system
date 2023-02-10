package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.sig.ecdsa.ECDSASignature;
import org.cryptimeleon.incentive.crypto.model.keys.store.StorePublicKey;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

import java.util.Objects;

public class SpendCouponSignature implements Representable {
    @Represented
    private ECDSASignature signature;
    @Represented
    private StorePublicKey storePublicKey;

    public SpendCouponSignature(ECDSASignature signature, StorePublicKey storePublicKey) {
        this.signature = signature;
        this.storePublicKey = storePublicKey;
    }

    public SpendCouponSignature(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    public ECDSASignature getSignature() {
        return signature;
    }

    public StorePublicKey getStorePublicKey() {
        return storePublicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpendCouponSignature that = (SpendCouponSignature) o;
        return Objects.equals(signature, that.signature) && Objects.equals(storePublicKey, that.storePublicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signature, storePublicKey);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
