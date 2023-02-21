package org.cryptimeleon.incentive.client.dto.provider;

import java.util.List;

public class BulkResultsProviderDto {
    private List<EarnResultProviderDto> earnResults;
    private List<SpendResultsProviderDto> spendResults;

    public BulkResultsProviderDto() {
    }

    public BulkResultsProviderDto(List<EarnResultProviderDto> earnResults, List<SpendResultsProviderDto> spendResults) {
        this.earnResults = earnResults;
        this.spendResults = spendResults;
    }

    public List<EarnResultProviderDto> getEarnResults() {
        return earnResults;
    }

    public List<SpendResultsProviderDto> getSpendResults() {
        return spendResults;
    }
}
