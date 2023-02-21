package org.cryptimeleon.incentive.client.dto.provider;


import java.util.List;

public class BulkRequestProviderDto {
    private List<SpendRequestProviderDto> spendRequests;
    private List<EarnRequestProviderDto> earnRequests;

    @SuppressWarnings("unused")
    public BulkRequestProviderDto() {
    }

    public BulkRequestProviderDto(List<SpendRequestProviderDto> spendRequests, List<EarnRequestProviderDto> earnRequests) {
        this.spendRequests = spendRequests;
        this.earnRequests = earnRequests;
    }

    public List<SpendRequestProviderDto> getSpendRequests() {
        return spendRequests;
    }

    public List<EarnRequestProviderDto> getEarnRequests() {
        return earnRequests;
    }
}
