package org.cryptimeleon.incentive.crypto.model;

import lombok.*;
import lombok.experimental.NonFinal;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * Data class representing a spend transaction.
 * Note that the challenge generation helper value gamma is contained in the double spending tag
 * and thus not a field of the transaction class (DRY principle).
 */
@Value
@Builder(toBuilder=true)
@AllArgsConstructor
public class Transaction implements Representable {
    @NonFinal
    @Represented
    Boolean isValid;

    @NonFinal
    @Represented(restorer = "Zn")
    Zn.ZnElement transactionID; // identifier for this transaction on protocol-level

    @NonFinal
    @Represented
    String userChoice; // string representing the reward the user wants to claim with this spend transaction

    @NonFinal
    @Represented
    BigInteger promotionId;

    @NonFinal
    DoubleSpendingTag dsTag; // data associated to a spend transaction that is required to trace double-spending, this contains challenge generation helper value gamma

    public Transaction() { }

    public Transaction(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this).register(pp.getBg().getZn(), "Zn").deserialize(repr.list().get(0));
        this.dsTag = new DoubleSpendingTag(repr.list().get(1), pp);
    }

    /**
     * Returns identifying information for this transaction,
     * consisting of the transaction ID and the challenge generator gamma of the associated double-spending tag.
     *
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
    public String toString() {
        return this.isValid.toString() + " "
                + this.transactionID.toString() + " "
                + this.userChoice + " "
                + this.dsTag.toString() + " ";
    }
}
