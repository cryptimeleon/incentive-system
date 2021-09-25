package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Simple data class representing the identifying information of a transaction.
 * This information is sent when querying the double-spending protection service for the containment of a specific transaction.
 */
@AllArgsConstructor
@Value
public class TransactionIdentifier implements Representable {
    @NonFinal
    @Represented(restorer = "zn")
    private Zn.ZnElement tid;

    @NonFinal
    @Represented(restorer = "zn")
    private Zn.ZnElement gamma;

    public TransactionIdentifier(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this).register(pp.getBg().getZn(), "zn").deserialize(repr);
    }

    public Representation getRepresentation() { return ReprUtil.serialize(this); }
}
