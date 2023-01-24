package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.sig.ecdsa.ECDSASignature;
import org.cryptimeleon.craco.sig.ecdsa.ECDSASignatureScheme;
import org.cryptimeleon.incentive.crypto.model.keys.store.StorePublicKey;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;

import java.util.Objects;

public class EarnStoreCoupon implements Representable {
    private final ECDSASignature signature;
    private final StorePublicKey storePublicKey;

    public EarnStoreCoupon(ECDSASignature signature, StorePublicKey storePublicKey) {
        this.signature = signature;
        this.storePublicKey = storePublicKey;
    }

    public EarnStoreCoupon(Representation representation) {
        ListRepresentation listRepresentation = (ListRepresentation) representation;
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        this.signature = (ECDSASignature) ecdsaSignatureScheme.restoreSignature(listRepresentation.get(0));
        this.storePublicKey = new StorePublicKey(listRepresentation.get(1));
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
        EarnStoreCoupon that = (EarnStoreCoupon) o;
        return Objects.equals(signature, that.signature) && Objects.equals(storePublicKey, that.storePublicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signature, storePublicKey);
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(signature.getRepresentation(), storePublicKey.getRepresentation());
    }
}
