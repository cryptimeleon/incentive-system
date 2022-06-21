package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentivesystem.dsprotectionservice.Util;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;

import javax.persistence.*;
import java.math.BigInteger;

/**
 * Data class representing a Spend-transaction.
 * Needs ID attribute and some annotations to be processable by Hibernate (ORM framework),
 * furthermore we need to record whether the transaction is still considered valid.
 * <p>
 * Note that some attributes are serialized representations of the resembled properties since Hibernate
 * can only marshall objects that have primitive datatype fields only.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "transactions")
public class TransactionEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private boolean isValid; // whether this transaction is still considered valid (can be invalidated over the course of double-spending protection)

    private String serializedTransactionIDRepr; // identifier for this transaction on protocol-level

    private String k; // number of points spent in this transaction

    private String promotionId;

    private long dsTagEntryId; // ID of the entry for the double-spending tag of the transaction (data associated to a spend transaction that is required to trace double-spending)

    private long producedDsidEntryId; // ID of the entry of the double-spending ID of the token produced in this transaction (realizes a transaction-token edge in the bipartite double-spending graph)

    private long consumedDsidEntryId; // ID of the entry of the double-spending ID of the token consumed in this transaction (realizes a token-transaction edge in the bipartite double-spending protection graph)

    /**
     * Default (i.e. no args) constructor needed for ORM reasons
     */
    public TransactionEntry() {
    }

    /**
     * Constructs a transaction entry from a serialized transaction representation (crypto object).
     * Note that the association with a double-spending tag is done subsequently.
     *
     * @param serializedTransaction serialized representation of a transaction
     * @param pp                    public parameters of the incentive system
     */
    public TransactionEntry(String serializedTransaction, IncentivePublicParameters pp) {
        // deserialize transaction object (from crypto)
        JSONConverter jsonConverter = new JSONConverter();
        Representation taRepresentation = jsonConverter.deserialize(serializedTransaction);
        Transaction ta = new Transaction(taRepresentation, pp);

        // create transaction entry object
        this.isValid = ta.getIsValid();
        this.serializedTransactionIDRepr = jsonConverter.serialize(ta.getTransactionID().getRepresentation());
        this.k = ta.getK().toString();
    }

    /**
     * All args constructor. Note that the ID  of the transaction is auto-generated.
     */
    public TransactionEntry(boolean isValid, String tid, String k, long dsTagEntryId, long producedDsidEntryID, long consumedDsidEntryId) {
        this.isValid = isValid;
        this.serializedTransactionIDRepr = tid;
        this.k = k;
        this.dsTagEntryId = dsTagEntryId;
        this.producedDsidEntryId = producedDsidEntryID;
        this.consumedDsidEntryId = consumedDsidEntryId;
    }

    /**
     * Auto-generates the entry for a transaction, links to consuming/consumed dsids as well as double-spending tags are not set.
     */
    public TransactionEntry(Transaction ta) {
        this.isValid = ta.getIsValid();
        this.serializedTransactionIDRepr = Util.computeSerializedRepresentation(ta.getTransactionID());
        this.k = ta.getK().toString();
    }

    /**
     * Constructor without IDs of produced and consumed tokens.
     */
    public TransactionEntry(boolean isValid, String tid, String k, long dsTagEntryId) {
        this.isValid = isValid;
        this.serializedTransactionIDRepr = tid;
        this.k = k;
        this.dsTagEntryId = dsTagEntryId;
    }

    /**
     * Marks this transaction invalid.
     */
    public void invalidate() {
        this.isValid = false;
    }
}
