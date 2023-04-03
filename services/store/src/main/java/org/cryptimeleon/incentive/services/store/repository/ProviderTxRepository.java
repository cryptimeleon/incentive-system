package org.cryptimeleon.incentive.services.store.repository;

import org.cryptimeleon.incentive.client.IncentiveClient;
import org.cryptimeleon.incentive.client.dto.EnrichedSpendTransactionDataDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProviderTxRepository {

    private final IncentiveClient incentiveClient;

    @Autowired
    public ProviderTxRepository(IncentiveClient incentiveClient) {
        this.incentiveClient = incentiveClient;
    }

    public void sendBasketTransactioData(BasketSpendTransactionData basketSpendTransactionData) {
        incentiveClient.sendSpendTransactionData(spendDataToDto(basketSpendTransactionData));
    }

    private EnrichedSpendTransactionDataDto spendDataToDto(BasketSpendTransactionData basketSpendTransactionData) {
        return new EnrichedSpendTransactionDataDto(
                basketSpendTransactionData.getSpendTransactionData(),
                basketSpendTransactionData.getPromotion().getPromotionParameters().getPromotionId(),
                basketSpendTransactionData.getTokenUpdate().getTokenUpdateId(),
                basketSpendTransactionData.getSerializedMetadata(),
                basketSpendTransactionData.getBasketPoinst());
    }
}
