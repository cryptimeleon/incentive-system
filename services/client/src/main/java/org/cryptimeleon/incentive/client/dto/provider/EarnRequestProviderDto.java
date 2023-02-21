package org.cryptimeleon.incentive.client.dto.provider;


import java.math.BigInteger;

public class EarnRequestProviderDto {
    private BigInteger promotionId;
    private String serializedEarnRequestECDSA;

    @SuppressWarnings("unused")
    public EarnRequestProviderDto() {
    }

    public EarnRequestProviderDto(BigInteger promotionId, String serializedEarnRequestECDSA) {
        this.promotionId = promotionId;
        this.serializedEarnRequestECDSA = serializedEarnRequestECDSA;
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public String getSerializedEarnRequestECDSA() {
        return serializedEarnRequestECDSA;
    }
}
