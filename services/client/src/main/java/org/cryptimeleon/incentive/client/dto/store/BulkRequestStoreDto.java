package org.cryptimeleon.incentive.client.dto.store;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BulkRequestStoreDto {
    private UUID basketId;
    private List<EarnRequestStoreDto> earnRequestStoreDtoList;
    private List<SpendRequestStoreDto> spendRequestStoreDtoList;

    @SuppressWarnings("unused")
    public BulkRequestStoreDto() {
    }

    public BulkRequestStoreDto(UUID basketId, List<EarnRequestStoreDto> earnRequestStoreDtoList, List<SpendRequestStoreDto> spendRequestStoreDtoList) {
        this.basketId = basketId;
        this.earnRequestStoreDtoList = earnRequestStoreDtoList;
        this.spendRequestStoreDtoList = spendRequestStoreDtoList;
    }

    public UUID getBasketId() {
        return basketId;
    }

    public List<EarnRequestStoreDto> getEarnRequestStoreDtoList() {
        return earnRequestStoreDtoList;
    }

    public List<SpendRequestStoreDto> getSpendRequestStoreDtoList() {
        return spendRequestStoreDtoList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BulkRequestStoreDto that = (BulkRequestStoreDto) o;
        return Objects.equals(basketId, that.basketId) && Objects.equals(earnRequestStoreDtoList, that.earnRequestStoreDtoList) && Objects.equals(spendRequestStoreDtoList, that.spendRequestStoreDtoList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basketId, earnRequestStoreDtoList, spendRequestStoreDtoList);
    }
}
