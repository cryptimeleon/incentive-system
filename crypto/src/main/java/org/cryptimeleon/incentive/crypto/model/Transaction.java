package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Data class representing a spend transaction.
 * Note that the challenge generation helper value gamma is contained in the double spending tag
 * and thus not a field of the transaction class (DRY principle).
 */
public class Transaction implements Representable {
    private final DoubleSpendingTag dsTag; // data associated to a spend transaction that is required to trace double-spending, this contains challenge generation helper value gamma
    @Represented
    private Boolean isValid;
    @Represented(restorer = "Zn")
    private Zn.ZnElement transactionID; // identifier for this transaction on protocol-level
    @Represented
    private String userChoice; // string representing the reward the user wants to claim with this spend transaction
    @Represented
    private BigInteger promotionId;

    public Transaction(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this).register(pp.getBg().getZn(), "Zn").deserialize(repr.list().get(0));
        this.dsTag = new DoubleSpendingTag(repr.list().get(1), pp);
    }

    public Transaction(Boolean isValid, Zn.ZnElement transactionID, String userChoice, BigInteger promotionId, DoubleSpendingTag dsTag) {
        this.isValid = isValid;
        this.transactionID = transactionID;
        this.userChoice = userChoice;
        this.promotionId = promotionId;
        this.dsTag = dsTag;
    }

    public static TransactionBuilder builder() {
        return new TransactionBuilder();
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

    public TransactionBuilder toBuilder() {
        return new TransactionBuilder().isValid(this.isValid).transactionID(this.transactionID).userChoice(this.userChoice).promotionId(this.promotionId).dsTag(this.dsTag);
    }

    public Boolean getIsValid() {
        return this.isValid;
    }

    public Zn.ZnElement getTransactionID() {
        return this.transactionID;
    }

    public String getUserChoice() {
        return this.userChoice;
    }

    public BigInteger getPromotionId() {
        return this.promotionId;
    }

    public DoubleSpendingTag getDsTag() {
        return this.dsTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(isValid, that.isValid) && Objects.equals(transactionID, that.transactionID) && Objects.equals(userChoice, that.userChoice) && Objects.equals(promotionId, that.promotionId) && Objects.equals(dsTag, that.dsTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isValid, transactionID, userChoice, promotionId, dsTag);
    }

    public static class TransactionBuilder {
        private Boolean isValid;
        private Zn.ZnElement transactionID;
        private String userChoice;
        private BigInteger promotionId;
        private DoubleSpendingTag dsTag;

        TransactionBuilder() {
        }

        public TransactionBuilder isValid(Boolean isValid) {
            this.isValid = isValid;
            return this;
        }

        public TransactionBuilder transactionID(Zn.ZnElement transactionID) {
            this.transactionID = transactionID;
            return this;
        }

        public TransactionBuilder userChoice(String userChoice) {
            this.userChoice = userChoice;
            return this;
        }

        public TransactionBuilder promotionId(BigInteger promotionId) {
            this.promotionId = promotionId;
            return this;
        }

        public TransactionBuilder dsTag(DoubleSpendingTag dsTag) {
            this.dsTag = dsTag;
            return this;
        }

        public Transaction build() {
            return new Transaction(isValid, transactionID, userChoice, promotionId, dsTag);
        }

        public String toString() {
            return "Transaction.TransactionBuilder(isValid=" + this.isValid + ", transactionID=" + this.transactionID + ", userChoice=" + this.userChoice + ", promotionId=" + this.promotionId + ", dsTag=" + this.dsTag + ")";
        }
    }
}
