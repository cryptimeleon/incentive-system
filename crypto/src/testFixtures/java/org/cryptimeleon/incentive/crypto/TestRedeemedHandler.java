package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.callback.IStoreBasketRedeemedHandler;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class TestRedeemedHandler implements IStoreBasketRedeemedHandler {
    HashMap<UUID, HashMap<BigInteger, byte[]>> redeemedBaskets = new HashMap<>();

    @Override
    public BasketRedeemState verifyAndRedeemRequestWithHash(UUID basketId, BigInteger promotionId, byte[] hash) {
        if (!redeemedBaskets.containsKey(basketId) || !redeemedBaskets.get(basketId).containsKey(promotionId)) {
            redeemedBaskets.putIfAbsent(basketId, new HashMap<>());
            redeemedBaskets.get(basketId).put(promotionId, hash);
            return BasketRedeemState.BASKET_NOT_REDEEMED;
        }
        if (Arrays.equals(redeemedBaskets.get(basketId).get(promotionId), hash)) {
            return BasketRedeemState.BASKED_REDEEMED_RETRY;
        }
        return BasketRedeemState.BASKET_REDEEMED_ABORT;
    }
}
