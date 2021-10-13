package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.math.hash.impl.ByteArrayAccumulator;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.HashIntoZn;
import org.cryptimeleon.math.structures.rings.zn.Zn;


/**
 * Collection of utility functions
 */
public class Util {

    static final int H_SIZE_WITHOUT_STORE = 6;

    /**
     * Hash function to retrieve ZnElement gamma in spend-deduct
     *
     * @param zn    ZnElement to retrieve
     * @param K     earn vector to hash
     * @param dsid  disid to hash
     * @param tid   tid to hash
     * @param cPre0 cPre0 to hash
     * @param cPre1 cPre1 to hash
     * @return hashed ZnElement gamma
     */
    public static Zn.ZnElement hashGamma(Zn zn, RingElementVector K, GroupElement dsid, Zn.ZnElement tid, GroupElement cPre0, GroupElement cPre1) {
        var hashfunction = new HashIntoZn(zn);
        var accumulator = new ByteArrayAccumulator();
        K.stream().forEachOrdered(k -> accumulator.escapeAndSeparate(k.getUniqueByteRepresentation()));
        accumulator.escapeAndSeparate(dsid.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(tid.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(cPre0.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(cPre1.getUniqueByteRepresentation());
        return hashfunction.hash(accumulator.extractBytes());
    }

    public static GroupElementVector constructH(ProviderPublicKey providerPublicKey, IncentivePublicParameters publicParameters, PromotionParameters promotionParameters) {
        return providerPublicKey.getH().prepend(publicParameters.getH7()).truncate(H_SIZE_WITHOUT_STORE + promotionParameters.getStoreSize());
    }

    public static GroupElementVector constructStorelessH(ProviderPublicKey providerPublicKey, IncentivePublicParameters publicParameters) {
        return providerPublicKey.getH().prepend(publicParameters.getH7()).truncate(H_SIZE_WITHOUT_STORE);
    }

    public static GroupElementVector constructStoreH(ProviderPublicKey providerPublicKey, IncentivePublicParameters publicParameters, PromotionParameters promotionParameters) {
        // the -1 is because we do not count h7
        return GroupElementVector.fromStream(providerPublicKey.getH().stream().skip(H_SIZE_WITHOUT_STORE - 1)).truncate(promotionParameters.getStoreSize());
    }

    public static RingElementVector constructStoreQ(ProviderSecretKey sk, PromotionParameters promotionParameters) {
        // the -1 is because we do not know the DLOG q7 to h7
        return RingElementVector.fromStream(sk.getQ().stream().skip(H_SIZE_WITHOUT_STORE - 1)).truncate(promotionParameters.getStoreSize());
    }
}
