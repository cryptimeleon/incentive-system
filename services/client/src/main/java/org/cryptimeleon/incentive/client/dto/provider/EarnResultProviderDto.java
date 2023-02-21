package org.cryptimeleon.incentive.client.dto.provider;

import java.math.BigInteger;

public class EarnResultProviderDto {
    private BigInteger promotionId;
    private String serializedEarnResponse;

    @SuppressWarnings("unused")
    public EarnResultProviderDto() {
    }

    public EarnResultProviderDto(BigInteger promotionId, String serializedEarnResponse) {
        this.promotionId = promotionId;
        this.serializedEarnResponse = serializedEarnResponse;
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public String getSerializedEarnResponse() {
        return serializedEarnResponse;
    }
}
