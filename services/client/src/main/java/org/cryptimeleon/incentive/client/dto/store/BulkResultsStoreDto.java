package org.cryptimeleon.incentive.client.dto.store;

import java.util.List;
import java.util.Objects;

public class BulkResultsStoreDto {
    private List<EarnResultStoreDto> earnResults;
    private List<SpendResultsStoreDto> spendResults;

    @SuppressWarnings("unused")
    public BulkResultsStoreDto() {
    }

    public BulkResultsStoreDto(List<EarnResultStoreDto> earnResults, List<SpendResultsStoreDto> spendResults) {
        this.earnResults = earnResults;
        this.spendResults = spendResults;
    }

    public List<EarnResultStoreDto> getEarnResults() {
        return earnResults;
    }

    public List<SpendResultsStoreDto> getSpendResults() {
        return spendResults;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BulkResultsStoreDto that = (BulkResultsStoreDto) o;
        return Objects.equals(earnResults, that.earnResults) && Objects.equals(spendResults, that.spendResults);
    }

    @Override
    public int hashCode() {
        return Objects.hash(earnResults, spendResults);
    }
}
