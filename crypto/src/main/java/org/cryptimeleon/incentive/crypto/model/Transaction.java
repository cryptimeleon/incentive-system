package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
@Getter
@AllArgsConstructor
public class Transaction implements Representable {
    @NonFinal
    @Represented
    private Boolean isValid;

    @NonFinal
    @Represented(restorer = "Zn")
    private Zn.ZnElement transactionID; // identifier for this transaction on protocol-level

    @NonFinal
    @Represented
    private BigInteger k; // number of points spent in this transaction ("spend amount")

    @NonFinal
    private DoubleSpendingTag dsTag; // data associated to a spend transaction that is required to trace double-spending, this contains challenge generation helper value gamma

    public Transaction(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this).register(pp.getBg().getZn(), "Zn").deserialize(repr.list().get(0));
        this.dsTag = new DoubleSpendingTag(repr.list().get(1), pp);
    }

    /**
     * Returns identifying information for this transaction,
     * consisting of the transaction ID and the challenge generator gamma of the associated double-spending tag.
     * @return TransactionIdentifier
     */
    public TransactionIdentifier getTaIdentifier() {
        return new TransactionIdentifier(
                this.transactionID,
                this.dsTag.getGamma()
        );
    }

    @Override
    public Representation getRepresentation() {
        ListRepresentation repr = new ListRepresentation();
        repr.add(ReprUtil.serialize(this));
        repr.add(dsTag.getRepresentation());
        return repr;
    }

    @Override
    public boolean equals(Object o) {
        if(!o.getClass().equals(Transaction.class)) {
            return false;
        }
        else {
            Transaction otherTa = (Transaction) o;
            return otherTa.getIsValid()== this.isValid
                    && otherTa.getTransactionID().equals(this.transactionID)
                    && otherTa.getK().equals(this.k)
                    && otherTa.getDsTag().equals(this.getDsTag());
        }
    }

    @Override
    public String toString() {
        return this.isValid.toString() + " "
                + this.transactionID.toString() + " "
                + this.k.toString() + " "
                + this.dsTag.toString() + " ";
    }
}
