package org.cryptimeleon.incentive.services.bootstrap;

import org.cryptimeleon.incentive.client.dto.BasketItemDto;
import org.cryptimeleon.incentive.client.dto.RewardItemDto;
import org.cryptimeleon.incentive.promotion.Promotion;

import java.util.List;

public class BootstrapData {
    public final BasketItemDto[] basketItems;
    public final RewardItemDto[] rewardItems;
    public final List<Promotion> promotions;

    public BootstrapData(BasketItemDto[] basketItems, RewardItemDto[] rewardItems, List<Promotion> promotions) {
        this.basketItems = basketItems;
        this.rewardItems = rewardItems;
        this.promotions = promotions;
    }
}
