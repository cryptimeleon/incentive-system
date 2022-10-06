package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.client.dto.inc.EarnTokenUpdateResultDto;
import org.cryptimeleon.incentive.client.dto.inc.TokenUpdateResult;
import org.cryptimeleon.incentive.client.dto.inc.ZkpTokenUpdateResultDto;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Repository that caches the results of token updates.
 * These updates are generated upon Spend time and held back until the user pays the basket.
 *
 * There are two types of such updates:
 * - earn updates: points that the user has earned for her basket
 * - ZKP token updates: rewards that the user has claimed using saved points
 */
@Repository
public class TokenUpdateResultRepository {
    /*
    * Token updates a stored as a map that maps baskets to applicable updates.
    * These applicable updates are again modelled as a map that maps promotion IDs to token updates.
    */
    private final Map<UUID, Map<BigInteger, TokenUpdateResult>> tokenUpdateResultMap;

    /**
     * Constructor, initializes repo with an empty map.
     */
    public TokenUpdateResultRepository() {
        tokenUpdateResultMap = new HashMap<>();
    }

    /**
     * Inserts a token update that is associated with the basket and promotion with the passed IDs.
     */
    private void insert(UUID basketId, BigInteger promotionId, TokenUpdateResult entity) {
        Map<BigInteger, TokenUpdateResult> map;
        if (tokenUpdateResultMap.containsKey(basketId)) {
            map = tokenUpdateResultMap.get(basketId);
        } else {
            map = new HashMap<>();
        }
        map.put(promotionId, entity);
        tokenUpdateResultMap.put(basketId, map);
    }

    /**
     * Inserts a ZKP token update that is associated with the basket and promotion with the passed IDs.
     */
    public void insertZkpTokenUpdateResponse(UUID basketId, BigInteger promotionId, UUID tokenUpdateId, String serializedZkpUpdateResponse) {
        insert(basketId, promotionId, new ZkpTokenUpdateResultDto(promotionId, tokenUpdateId, serializedZkpUpdateResponse));
    }

    /**
     * Inserts an earn token update that is associated with the basket and promotion with the passed IDs.
     */
    public void insertEarnResponse(UUID basketId, BigInteger promotionId, String serializedEarnResponse) {
        insert(basketId, promotionId, new EarnTokenUpdateResultDto(promotionId, serializedEarnResponse));
    }

    /**
     * Returns all token updates that were generated based on the basket with the passed ID.
     */
    public Map<BigInteger, TokenUpdateResult> getUpdateResults(UUID basketId) {
        return tokenUpdateResultMap.get(basketId);
    }
}

