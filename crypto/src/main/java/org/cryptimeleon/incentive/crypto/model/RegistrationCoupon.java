package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.sig.ecdsa.ECDSASignature;
import org.cryptimeleon.incentive.crypto.IncentiveSystemRestorer;
import org.cryptimeleon.incentive.crypto.model.keys.store.StorePublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

import java.util.Objects;

public class RegistrationCoupon implements Representable {
    @Represented(restorer = IncentiveSystemRestorer.RESTORER_NAME)
    private UserPublicKey userPublicKey;
    @Represented
    private String userInfo;
    @Represented
    private StorePublicKey storePublicKey;
    @Represented
    private ECDSASignature signature;

    public RegistrationCoupon(UserPublicKey userPublicKey, String userInfo, StorePublicKey storePublicKey, ECDSASignature signature) {
        this.userPublicKey = userPublicKey;
        this.userInfo = userInfo;
        this.storePublicKey = storePublicKey;
        this.signature = signature;
    }

    public RegistrationCoupon(Representation representation, IncentiveSystemRestorer incentiveSystemRestorer) {
        (new ReprUtil(this))
                .register(incentiveSystemRestorer, IncentiveSystemRestorer.RESTORER_NAME)
                .deserialize(representation);
    }

    public UserPublicKey getUserPublicKey() {
        return userPublicKey;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public StorePublicKey getStorePublicKey() {
        return storePublicKey;
    }

    public ECDSASignature getSignature() {
        return signature;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationCoupon that = (RegistrationCoupon) o;
        return Objects.equals(userPublicKey, that.userPublicKey) && Objects.equals(userInfo, that.userInfo) && Objects.equals(storePublicKey, that.storePublicKey) && Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPublicKey, userInfo, storePublicKey, signature);
    }
}
