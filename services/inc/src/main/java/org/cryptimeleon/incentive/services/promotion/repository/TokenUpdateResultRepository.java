package org.cryptimeleon.incentive.services.promotion.repository;

import org.cryptimeleon.incentive.client.dto.inc.EarnTokenUpdateResultDto;
import org.cryptimeleon.incentive.client.dto.inc.TokenUpdateResult;
import org.cryptimeleon.incentive.client.dto.inc.ZkpTokenUpdateResultDto;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// This is the cache for results of token updates that are held back util the user pays the basket.
@Repository
public class TokenUpdateResultRepository {
    private Map<UUID, Map<BigInteger, TokenUpdateResult>> tokenUpdateResultMap;

    public TokenUpdateResultRepository() {
        tokenUpdateResultMap = new HashMap<>();
    }

    public void insertZkpTokenUpdateResponse(UUID basketId, BigInteger promotionId, UUID tokenUpdateId, String serializedZkpUpdateResponse) {
        insert(basketId, promotionId, new ZkpTokenUpdateResultDto(promotionId, tokenUpdateId, serializedZkpUpdateResponse));
    }

    public void insertEarnResponse(UUID basketId, BigInteger promotionId, String serializedEarnResponse) {
        insert(basketId, promotionId, new EarnTokenUpdateResultDto(promotionId, serializedEarnResponse));
    }

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

    public Map<BigInteger, TokenUpdateResult> getUpdateResults(UUID basketId) {
        return tokenUpdateResultMap.get(basketId);
    }
}

