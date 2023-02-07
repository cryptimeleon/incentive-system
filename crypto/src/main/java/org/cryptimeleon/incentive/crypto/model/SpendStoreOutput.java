package org.cryptimeleon.incentive.crypto.model;

public class SpendStoreOutput {
    public final SpendCouponSignature spendCouponSignature;
    public final SpendClearingData spendClearingData;

    public SpendStoreOutput(SpendCouponSignature spendCouponSignature, SpendClearingData spendClearingData) {
        this.spendCouponSignature = spendCouponSignature;
        this.spendClearingData = spendClearingData;
    }
}
