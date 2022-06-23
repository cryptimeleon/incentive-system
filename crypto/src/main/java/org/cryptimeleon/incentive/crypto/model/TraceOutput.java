package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/**
 * Simple data class that represents outputs of the trace algorithm from the Cryptimeleon incentive system paper
 * when called on the data of some transaction T.
 */
@Value
@AllArgsConstructor
public class TraceOutput {
    /**
     * Remainder token of T
     */
    GroupElement dsidStar;

    /**
     * ElGamal secret key associated to the remainder token of T,
     * i.e. the corresponding secret key associated to the double-spending ID (= ElGamal public key)
     * that is consumed by a transaction T' that follows T.
     */
    ZnElement dsTraceStar;
}
