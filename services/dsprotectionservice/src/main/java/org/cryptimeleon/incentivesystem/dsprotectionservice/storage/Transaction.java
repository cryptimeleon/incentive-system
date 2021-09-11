package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

import javax.persistence.*;
import java.math.BigInteger;

/**
 * Data class representing a Spend-transaction.
 * Needs ID attribute and some annotations to be processable by Hibernate (ORM framework), furthermore we need to record whether the transaction is still considered valid.
 * The counterpart transaction class in the crypto project does not have these (since on the crypto side, all transactions are considered in isolation)
 * but apart from that, the two classes are identical.
 */
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private boolean isValid; // whether this transaction is still considered valid (can be invalidated over the course of double-spending protection)

    private ZnElement transactionID; // identifier for this transaction on protocol-level

    private BigInteger k; // number of points spent in this transaction

    private DoubleSpendingTag dsTag; // data associated to a spend transaction that is required to trace double-spending

    public Transaction() {}

    public Transaction(long id, boolean isValid, ZnElement tid, BigInteger k, DoubleSpendingTag dsTag){
        this.id = id;
        this.isValid = isValid;
        this.transactionID = tid;
        this.k = k;
        this.dsTag = dsTag;
    }

    public void invalidate() {
        this.isValid = false;
    }

    public String toString(){
        return this.id + " " + this.transactionID.toString() + " " + this.k.toString() + " " + this.dsTag.toString();
    }
}
