package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import lombok.Getter;
import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

import javax.persistence.*;
import java.math.BigInteger;

/**
 * Data class representing a Spend-transaction.
 * Needs ID attribute and some annotations to be processable by Hibernate (ORM framework),
 * furthermore we need to record whether the transaction is still considered valid.
 */
@Entity
@Getter
@Table(name = "transactions")
public class TransactionEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private boolean isValid; // whether this transaction is still considered valid (can be invalidated over the course of double-spending protection)

    private ZnElement transactionID; // identifier for this transaction on protocol-level

    private BigInteger k; // number of points spent in this transaction

    private DsTagEntry dsTagEntry; // data associated to a spend transaction that is required to trace double-spending

    public TransactionEntry() {}

    /**
     * Constructs a transaction entry from a serialized transaction representation (crypto object).
     * @param serializedTransaction serialized representation of a transaction
     * @param pp public parameters of the incentive system
     */
    public TransactionEntry(String serializedTransaction, IncentivePublicParameters pp) {
        // deserialize transaction object (from crypto)
        JSONConverter jsonConverter = new JSONConverter();
        Representation taRepresentation = jsonConverter.deserialize(serializedTransaction);
        Transaction ta = new Transaction(taRepresentation, pp);

        // create transaction entry object
        this.isValid = ta.isValid();
        this.transactionID = ta.getTransactionID();
        this.k = ta.getK();
        DoubleSpendingTag dsTag = ta.getDsTag();
        this.dsTagEntry = new DsTagEntry(
                dsTag.getC0(),
                dsTag.getC1(),
                dsTag.getGamma(),
                dsTag.getEskStarProv(),
                dsTag.getCtrace0(),
                dsTag.getCtrace1()
        );
    }

    /**
     * All-args constructor, note that id is auto-generated.
     */
    public TransactionEntry(boolean isValid, ZnElement tid, BigInteger k, DsTagEntry dsTag){
        this.isValid = isValid;
        this.transactionID = tid;
        this.k = k;
        this.dsTagEntry = dsTag;
    }

    /**
     * Marks transaction invalid.
     */
    public void invalidate() {
        this.isValid = false;
    }

    public String toString(){
        return this.id + " " + this.transactionID.toString() + " " + this.k.toString() + " " + this.dsTagEntry.toString();
    }
}
