package org.cryptimeleon.incentive.client.dto.provider;

import java.util.List;

public class BulkResultsProviderDto {
    private List<EarnResultProviderDto> earnResults;
    private List<SpendResultProviderDto> spendResults;

    public BulkResultsProviderDto() {
    }

    public BulkResultsProviderDto(List<EarnResultProviderDto> earnResults, List<SpendResultProviderDto> spendResults) {
        this.earnResults = earnResults;
        this.spendResults = spendResults;
    }

    public List<EarnResultProviderDto> getEarnResults() {
        return earnResults;
    }

    public List<SpendResultProviderDto> getSpendResults() {
        return spendResults;
    }
}
