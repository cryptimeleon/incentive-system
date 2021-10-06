package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * Data class representing a spend transaction.
 * Note that the challenge generation helper value gamma is contained in the double spending tag
 * and thus not a field of the transaction class (DRY principle).
 */
@Value
@AllArgsConstructor
public class Transaction implements Representable {
    @NonFinal
    @Represented
    private boolean isValid;

    @NonFinal
    @Represented(restorer = "Zn")
    private Zn.ZnElement transactionID; // identifier for this transaction on protocol-level

    @NonFinal
    @Represented
    private BigInteger k; // number of points spent in this transaction ("spend amount")

    @NonFinal
    private DoubleSpendingTag dsTag; // data associated to a spend transaction that is required to trace double-spending, this contains challenge generation helper value gamma

    public Transaction(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this).register(pp.getBg().getZn(), "zn").deserialize(repr.list().get(0));
        this.dsTag = new DoubleSpendingTag(repr.list().get(1), pp);
    }

    /**
     * All args constructor, all parameters with complex data types are passed as serialized representations.
     */
    public Transaction(IncentivePublicParameters pp, boolean isValid, String serializedTransactionIDRepr, String k,
                       String serializedC0Repr, String serializedC1Repr, String serializedGammaRepr, String serializedEskProvStarRepr, String serializedCTrace0Repr, String serializedCTrace1Repr) {
        this.isValid = isValid;

        JSONConverter jsonConverter = new JSONConverter();
        Representation transactionIDRepr = jsonConverter.deserialize(serializedTransactionIDRepr);
        this.transactionID = pp.getBg().getZn().restoreElement(transactionIDRepr);

        this.k = new BigInteger(k);
    }

    @Override
    public Representation getRepresentation() {
        ListRepresentation repr = new ListRepresentation();
        repr.add(ReprUtil.serialize(this));
        repr.add(dsTag.getRepresentation());
        return repr;
    }
}
