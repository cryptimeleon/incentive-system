package org.cryptimeleon.incentive.services.bootstrap;

import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.dto.ItemDto;
import org.cryptimeleon.incentive.client.dto.RewardItemDto;
import org.cryptimeleon.incentive.promotion.Promotion;

import java.util.List;

public class BootstrapBasketClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BootstrapBasketClient.class);
    final String basketServiceProviderSecret;
    final BasketClient basketClient;

    public BootstrapBasketClient(String basketServiceProviderSecret, BasketClient basketClient) {
        this.basketServiceProviderSecret = basketServiceProviderSecret;
        this.basketClient = basketClient;
    }

    public void publishBootstrapData(BootstrapDataChoice bootstrapDataChoice) {
        BootstrapData bootstrapData = getBootstrapDataFor(bootstrapDataChoice);
        publishBasketItems(bootstrapData.basketItems);
        publishRewardItems(bootstrapData.rewardItems);
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

    private void publishBasketItems(ItemDto[] basketItems) {
        for (ItemDto item : basketItems) {
            log.info("adding " + item);
            basketClient.newBasketItem(item, basketServiceProviderSecret).block();
        }
    }

    private void publishRewardItems(RewardItemDto[] rewardItems) {
        for (RewardItemDto rewardItem : rewardItems) {
            log.info("adding " + rewardItem);
            basketClient.newRewardItem(rewardItem, basketServiceProviderSecret).block();
        }
    }

    private void publishPromotions(List<Promotion> promotions) {
        basketClient.addPromotions(promotions, basketServiceProviderSecret).block();
    }
}
