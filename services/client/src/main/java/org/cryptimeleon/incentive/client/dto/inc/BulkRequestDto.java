package org.cryptimeleon.incentive.client.dto.inc;

import java.util.List;
import java.util.Objects;

public final class BulkRequestDto {
    private final List<EarnRequestDto> earnRequestDtoList;
    private final List<SpendRequestDto> spendRequestDtoList;

    public BulkRequestDto(final List<EarnRequestDto> earnRequestDtoList, final List<SpendRequestDto> spendRequestDtoList) {
        this.earnRequestDtoList = earnRequestDtoList;
        this.spendRequestDtoList = spendRequestDtoList;
    }

    public List<EarnRequestDto> getEarnRequestDtoList() {
        return this.earnRequestDtoList;
    }

    public List<SpendRequestDto> getSpendRequestDtoList() {
        return this.spendRequestDtoList;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BulkRequestDto)) return false;
        final BulkRequestDto other = (BulkRequestDto) o;
        final Object this$earnRequestDtoList = this.getEarnRequestDtoList();
        final Object other$earnRequestDtoList = other.getEarnRequestDtoList();
        if (!Objects.equals(this$earnRequestDtoList, other$earnRequestDtoList))
            return false;
        final Object this$spendRequestDtoList = this.getSpendRequestDtoList();
        final Object other$spendRequestDtoList = other.getSpendRequestDtoList();
        return Objects.equals(this$spendRequestDtoList, other$spendRequestDtoList);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $earnRequestDtoList = this.getEarnRequestDtoList();
        result = result * PRIME + ($earnRequestDtoList == null ? 43 : $earnRequestDtoList.hashCode());
        final Object $spendRequestDtoList = this.getSpendRequestDtoList();
        result = result * PRIME + ($spendRequestDtoList == null ? 43 : $spendRequestDtoList.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "BulkRequestDto(earnRequestDtoList=" + this.getEarnRequestDtoList() + ", spendRequestDtoList=" + this.getSpendRequestDtoList() + ")";
    }
}
