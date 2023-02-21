package org.cryptimeleon.incentive.services.basket.repository;

import org.cryptimeleon.incentive.client.dto.store.BulkResultsStoreDto;
import org.cryptimeleon.incentive.services.basket.StoreException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.UUID;

/**
 * Essentially a cache for the earn/spend responses that are held back until the corresponding basket is paid.
 */
@Repository
public class BulkResponseRepository {
    private final HashMap<UUID, BulkResultsStoreDto> responseForBasketMap = new HashMap<>();

    public BulkResultsStoreDto removeBulkResultFor(UUID basketId) {
        if (!responseForBasketMap.containsKey(basketId)) {
            throw new StoreException(String.format("No bulk results for basket with id %s found!", basketId));
        }
        return responseForBasketMap.remove(basketId);
    }

    public void addBulkResult(UUID basketId, BulkResultsStoreDto bulkResultsStoreDto) {
        responseForBasketMap.put(basketId, bulkResultsStoreDto);
    }
}
