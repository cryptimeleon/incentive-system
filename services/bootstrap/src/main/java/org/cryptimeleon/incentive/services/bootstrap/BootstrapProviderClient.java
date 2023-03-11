package org.cryptimeleon.incentive.services.bootstrap;

import org.cryptimeleon.incentive.client.IncentiveClient;
import org.cryptimeleon.incentive.promotion.Promotion;

import java.util.List;

public class BootstrapProviderClient {
    final String promotionServiceProviderSecret;
    final IncentiveClient incentiveClient;

    public BootstrapProviderClient(String promotionServiceProviderSecret, IncentiveClient incentiveClient) {
        this.promotionServiceProviderSecret = promotionServiceProviderSecret;
        this.incentiveClient = incentiveClient;
    }

    public void publishBootstrapData(BootstrapDataChoice bootstrapDataChoice) {
        BootstrapData bootstrapData = getBootstrapDataFor(bootstrapDataChoice);
        publishPromotions(bootstrapData.promotions);
    }

    private BootstrapData getBootstrapDataFor(BootstrapDataChoice bootstrapDataChoice) {
        switch (bootstrapDataChoice) {
            case DEFAULT:
                return DefaultBootstrapData.bootstrapData;
            case DEMO:
                return DemoBootstrapData.bootstrapData;
            default:
                throw new RuntimeException("Could not find bootstrap data for BootstrapDataChoice" + bootstrapDataChoice.name());
        }
    }

    private void publishPromotions(List<Promotion> promotions) {
        incentiveClient.addPromotions(promotions, promotionServiceProviderSecret).block();
    }
}
