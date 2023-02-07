package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.incentive.crypto.model.SpendCouponSignature;
import org.cryptimeleon.incentive.crypto.model.SpendStoreOutput;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Interface for the logic around redeeming baskets and re-requesting with the same hash.
 */
public interface IStoreBasketRedeemedHandler {
    /**
     * Returns true if:
     * - promotionId unused or
     * - promotionId used with same hash
     * Stores promotionId, hash tuple
     * <p>
     * This is a single method (and not one read and one write access) to allow atomic reading and writing of redeemed
     * baskets to prevent attacks.
     *
     * @param basketId the associated basket
     * @param promotionId determines the promotion
     * @param hash        is used to enable re-requesting failed updates
     * @return true if the store should issue an ECDSA signature for the request
     */
    boolean verifyAndStorePromotionIdAndHashForBasket(UUID basketId, BigInteger promotionId, byte[] hash);

    BasketRedeemedResult verifyAndRedeemBasket(UUID basketId, BigInteger promotionId, Zn.ZnElement gamma, SpendCouponSignature signature);

    interface BasketRedeemedResult {}

    class BasketNotRedeemed implements BasketRedeemedResult {}
    class BasketRedeemedForDifferentGamma implements BasketRedeemedResult {}
    class BasketRedeemedForSameGamma implements BasketRedeemedResult {
        public final SpendStoreOutput spendStoreOutput;

        BasketRedeemedForSameGamma(SpendStoreOutput spendStoreOutput) {
            this.spendStoreOutput = spendStoreOutput;
        }
    }
}

