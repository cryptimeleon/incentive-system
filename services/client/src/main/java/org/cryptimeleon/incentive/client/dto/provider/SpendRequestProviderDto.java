package org.cryptimeleon.incentive.client.dto.provider;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class SpendRequestProviderDto {
    private BigInteger promotionId;
    private String serializedSpendRequest;
    private String serializedTokenUpdateMetadata;
    private UUID basketId;
    private UUID tokenUpdateId;
    private List<BigInteger> basketPoints;

    @SuppressWarnings("unused")
    public SpendRequestProviderDto() {
    }

    public SpendRequestProviderDto(BigInteger promotionId, String serializedSpendRequest, String serializedTokenUpdateMetadata, UUID basketId, UUID tokenUpdateId, List<BigInteger> basketPoints) {
        this.promotionId = promotionId;
        this.serializedSpendRequest = serializedSpendRequest;
        this.serializedTokenUpdateMetadata = serializedTokenUpdateMetadata;
        this.basketId = basketId;
        this.tokenUpdateId = tokenUpdateId;
        this.basketPoints = basketPoints;
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public String getSerializedSpendRequest() {
        return serializedSpendRequest;
    }

    public String getSerializedTokenUpdateMetadata() {
        return serializedTokenUpdateMetadata;
    }

    public UUID getBasketId() {
        return basketId;
    }

    public UUID getTokenUpdateId() {
        return tokenUpdateId;
    }

    public List<BigInteger> getBasketPoints() {
        return basketPoints;
    }
}
