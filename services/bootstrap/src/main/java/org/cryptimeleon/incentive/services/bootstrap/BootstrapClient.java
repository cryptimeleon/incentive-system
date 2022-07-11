package org.cryptimeleon.incentive.services.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.IncentiveClient;
import org.cryptimeleon.incentive.client.dto.BasketItemDto;
import org.cryptimeleon.incentive.client.dto.RewardItemDto;
import org.cryptimeleon.incentive.promotion.Promotion;

import java.util.List;

@Slf4j
public class BootstrapClient {

    String basketServiceProviderSecret;
    String promotionServiceProviderSecret;
    BasketClient basketClient;
    IncentiveClient incentiveClient;

    public BootstrapClient(String basketServiceProviderSecret, String promotionServiceProviderSecret, BasketClient basketClient, IncentiveClient incentiveClient) {
        this.basketServiceProviderSecret = basketServiceProviderSecret;
        this.promotionServiceProviderSecret = promotionServiceProviderSecret;
        this.basketClient = basketClient;
        this.incentiveClient = incentiveClient;
    }

    public void publishBootstrapData(BootstrapData bootstrapData) {
        publishBasketItems(bootstrapData.basketItems);
        publishRewardItems(bootstrapData.rewardItems);
        publishPromotions(bootstrapData.promotions);
    }

    private void publishBasketItems(BasketItemDto[] basketItems) {
        for (BasketItemDto item : basketItems) {
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
        incentiveClient.addPromotions(promotions, promotionServiceProviderSecret).block();
    }
}
