package org.cryptimeleon.incentive.services.dsprotection.storage;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.services.dsprotection.Util;

import javax.persistence.*;

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

    private String userChoice; // string describing/identifying the reward that the user wants to claim with this spend transaction ("teddy bear", "vip bronze", ...)

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
     * Auto-generates the entry for a transaction, links to consuming/consumed dsids as well as double-spending tags are not set.
     */
    public TransactionEntry(Transaction ta) {
        this.isValid = ta.getIsValid();
        this.serializedTransactionIDRepr = Util.computeSerializedRepresentation(ta.getTransactionID());
        this.userChoice = ta.getUserChoice();
        this.promotionId = ta.getPromotionId().toString();
    }

    /**
     * Marks this transaction invalid.
     */
    public void invalidate() {
        this.isValid = false;
    }
}
