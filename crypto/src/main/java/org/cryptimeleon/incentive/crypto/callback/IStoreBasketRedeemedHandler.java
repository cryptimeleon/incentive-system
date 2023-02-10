package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Interface for the logic around redeeming baskets and re-requesting with the same hash.
 */
public interface IStoreBasketRedeemedHandler {

    /**
     * Default method with same functionality as {@link #verifyAndRedeemRequestWithHash(UUID, BigInteger, byte[]) verifyAndRedeemRequestWithHash}
     * to be used in Earn.
     * <p>
     * Do not overwrite this!
     */
    default BasketRedeemState verifyAndRedeemBasketEarn(UUID basketId, BigInteger promotionId, byte[] hash) {
        return verifyAndRedeemRequestWithHash(basketId, promotionId, hash);
    }


    /**
     * Default method with same functionality as {@link #verifyAndRedeemRequestWithHash(UUID, BigInteger, byte[]) verifyAndRedeemRequestWithHash}
     * but with type ZnElement for {@literal gamma}.
     * <p>
     * Do not overwrite this!
     */
    default BasketRedeemState verifyAndRedeemBasketSpend(UUID basketId, BigInteger promotionId, Zn.ZnElement gamma) {
        return verifyAndRedeemRequestWithHash(basketId, promotionId, gamma.getUniqueByteRepresentation());
    }

    /**
     * Checks whether there is already some hash associated with a (basket, promotionId) tuple.
     * If not, it returns {@link BasketRedeemState#BASKET_NOT_REDEEMED}.
     * Otherwise, the hash is compared with the parameter {@literal  hash}. If they are the same, return
     * {@link BasketRedeemState#BASKED_REDEEMED_RETRY}, else {@link BasketRedeemState#BASKET_REDEEMED_ABORT}.
     */
    BasketRedeemState verifyAndRedeemRequestWithHash(UUID basketId, BigInteger promotionId, byte[] hash);

    enum BasketRedeemState {
        BASKET_NOT_REDEEMED,
        BASKED_REDEEMED_RETRY,
        BASKET_REDEEMED_ABORT
    }
}

