package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

import javax.persistence.*;
import java.math.BigInteger;

/**
 * Data class representing a Spend-transaction.
 */
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private ZnElement transactionID; // identifier for this transaction on protocol-level
    private ZnElement gamma; // challenge generation helper value

    private BigInteger k; // number of points spent in this transaction

    private DoubleSpendingTag dsTag; // data associated to a spend transaction that is required to trace double-spending

    public Transaction() {}

    public Transaction(long id, ZnElement tid, ZnElement gamma, BigInteger k, DoubleSpendingTag dsTag){
        this.id = id;
        this.transactionID = tid;
        this.gamma = gamma;
        this.k = k;
        this.dsTag = dsTag;
    }

    public String toString(){
        return this.id + " " + this.transactionID.toString() + " " + this.gamma.toString() + " " + this.k.toString() + " " + this.dsTag;
    }
}
