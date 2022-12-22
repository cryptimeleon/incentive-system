package org.cryptimeleon.incentive.crypto.model;

import java.util.Objects;

/**
 * Needed for testing the double-spending protection service.
 * Capsulates the resulting token and transaction from spend transaction.
 */
public class SpendDeductOutput {
    private final Token resultToken; // the token resulting from the spend-deduct protocol
    private final Transaction occuredTransaction; // the transaction object capsulating the data of the occurred transaction

    public SpendDeductOutput(Token resultToken, Transaction occuredTransaction) {
        this.resultToken = resultToken;
        this.occuredTransaction = occuredTransaction;
    }

    public Token getResultToken() {
        return this.resultToken;
    }

    public Transaction getOccuredTransaction() {
        return this.occuredTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpendDeductOutput that = (SpendDeductOutput) o;
        return Objects.equals(resultToken, that.resultToken) && Objects.equals(occuredTransaction, that.occuredTransaction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultToken, occuredTransaction);
    }

    public String toString() {
        return "SpendDeductOutput(resultToken=" + this.getResultToken() + ", occuredTransaction=" + this.getOccuredTransaction() + ")";
    }
}
