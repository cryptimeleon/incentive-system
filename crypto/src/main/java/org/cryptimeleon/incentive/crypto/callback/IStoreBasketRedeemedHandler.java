package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Interface for the logic around redeeming baskets and re-requesting with the same hash.
 */
public interface IStoreBasketRedeemedHandler {

    default BasketRedeemState verifyAndRedeemBasketEarn(UUID basketId, BigInteger promotionId, byte[] hash) {
        return verifyAndRedeemRequestWithHash(basketId, promotionId, hash);
    }

    // Compare the promotionId, gamma tuple at basket
    default BasketRedeemState verifyAndRedeemBasketSpend(UUID basketId, BigInteger promotionId, Zn.ZnElement gamma) {
        return verifyAndRedeemRequestWithHash(basketId, promotionId, gamma.getUniqueByteRepresentation());
    }

    BasketRedeemState verifyAndRedeemRequestWithHash(UUID basketId, BigInteger promotionId, byte[] hash);

    enum BasketRedeemState {
        BASKET_NOT_REDEEMED,
        BASKED_REDEEMED_RETRY,
        BASKET_REDEEMED_ABORT
    }
}

