package org.cryptimeleon.incentive.services.provider.api;

public class RegistrationCouponJSON {
    private final String userInfo;
    private final String userPublicKey;
    private final String signature;
    private final String storePublicKey;

    public RegistrationCouponJSON(String userInfo, String userPublicKey, String signature, String storePublicKey) {
        this.userInfo = userInfo;
        this.userPublicKey = userPublicKey;
        this.signature = signature;
        this.storePublicKey = storePublicKey;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public String getUserPublicKey() {
        return userPublicKey;
    }

    public String getSignature() {
        return signature;
    }

    public String getStorePublicKey() {
        return storePublicKey;
    }
}
