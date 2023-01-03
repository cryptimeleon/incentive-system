package org.cryptimeleon.incentive.services.dsprotection.storage;

import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.services.dsprotection.Util;

import javax.persistence.*;
import java.util.Objects;

/**
 * Data class representing a Spend-transaction.
 * Needs ID attribute and some annotations to be processable by Hibernate (ORM framework),
 * furthermore we need to record whether the transaction is still considered valid.
 * <p>
 * Note that some attributes are serialized representations of the resembled properties since Hibernate
 * can only marshall objects that have primitive datatype fields only.
 */
@Entity
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

    public long getId() {
        return this.id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public String getSerializedTransactionIDRepr() {
        return this.serializedTransactionIDRepr;
    }

    public String getUserChoice() {
        return this.userChoice;
    }

    public String getPromotionId() {
        return this.promotionId;
    }

    public long getDsTagEntryId() {
        return this.dsTagEntryId;
    }

    public void setDsTagEntryId(final long dsTagEntryId) {
        this.dsTagEntryId = dsTagEntryId;
    }

    public long getProducedDsidEntryId() {
        return this.producedDsidEntryId;
    }

    public void setProducedDsidEntryId(final long producedDsidEntryId) {
        this.producedDsidEntryId = producedDsidEntryId;
    }

    public long getConsumedDsidEntryId() {
        return this.consumedDsidEntryId;
    }

    public void setConsumedDsidEntryId(final long consumedDsidEntryId) {
        this.consumedDsidEntryId = consumedDsidEntryId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TransactionEntry)) return false;
        final TransactionEntry other = (TransactionEntry) o;
        if (!other.canEqual(this)) return false;
        if (this.getId() != other.getId()) return false;
        if (this.isValid() != other.isValid()) return false;
        if (this.getDsTagEntryId() != other.getDsTagEntryId()) return false;
        if (this.getProducedDsidEntryId() != other.getProducedDsidEntryId()) return false;
        if (this.getConsumedDsidEntryId() != other.getConsumedDsidEntryId()) return false;
        final Object this$serializedTransactionIDRepr = this.getSerializedTransactionIDRepr();
        final Object other$serializedTransactionIDRepr = other.getSerializedTransactionIDRepr();
        if (!Objects.equals(this$serializedTransactionIDRepr, other$serializedTransactionIDRepr))
            return false;
        final Object this$userChoice = this.getUserChoice();
        final Object other$userChoice = other.getUserChoice();
        if (!Objects.equals(this$userChoice, other$userChoice))
            return false;
        final Object this$promotionId = this.getPromotionId();
        final Object other$promotionId = other.getPromotionId();
        return Objects.equals(this$promotionId, other$promotionId);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TransactionEntry;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $id = this.getId();
        result = result * PRIME + (int) ($id >>> 32 ^ $id);
        result = result * PRIME + (this.isValid() ? 79 : 97);
        final long $dsTagEntryId = this.getDsTagEntryId();
        result = result * PRIME + (int) ($dsTagEntryId >>> 32 ^ $dsTagEntryId);
        final long $producedDsidEntryId = this.getProducedDsidEntryId();
        result = result * PRIME + (int) ($producedDsidEntryId >>> 32 ^ $producedDsidEntryId);
        final long $consumedDsidEntryId = this.getConsumedDsidEntryId();
        result = result * PRIME + (int) ($consumedDsidEntryId >>> 32 ^ $consumedDsidEntryId);
        final Object $serializedTransactionIDRepr = this.getSerializedTransactionIDRepr();
        result = result * PRIME + ($serializedTransactionIDRepr == null ? 43 : $serializedTransactionIDRepr.hashCode());
        final Object $userChoice = this.getUserChoice();
        result = result * PRIME + ($userChoice == null ? 43 : $userChoice.hashCode());
        final Object $promotionId = this.getPromotionId();
        result = result * PRIME + ($promotionId == null ? 43 : $promotionId.hashCode());
        return result;
    }
}
