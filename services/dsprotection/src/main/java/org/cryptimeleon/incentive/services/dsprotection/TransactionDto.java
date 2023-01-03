package org.cryptimeleon.incentive.services.dsprotection;

import org.cryptimeleon.incentive.crypto.model.Transaction;

import java.util.Objects;

/**
 * A data transfer object representing a transaction as it is treated in the frontend.
 * So a transaction DTO consists of a validity flag, a transaction ID
 * and a string representing the reward that the user wants to claim with this transaction.
 * </br>
 * We decided to not display the double-spending tag in the frontend to avoid obfuscation.
 */
public class TransactionDto {
    private final boolean isValid; // validity state of this transaction
    private final String tid; // transaction ID
    private final String pid; // promotion ID of the promotion that the user takes part in
    private final String userChoice; // string representing the reward that the user wanted to claim with this transaction

    public TransactionDto(Transaction ta) {
        this.isValid = ta.getIsValid();
        this.tid = ta.getTransactionID().toString();
        this.pid = ta.getPromotionId().toString();
        this.userChoice = ta.getUserChoice();
    }

    public boolean isValid() {
        return this.isValid;
    }

    public String getTid() {
        return this.tid;
    }

    public String getPid() {
        return this.pid;
    }

    public String getUserChoice() {
        return this.userChoice;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TransactionDto)) return false;
        final TransactionDto other = (TransactionDto) o;
        if (!other.canEqual(this)) return false;
        if (this.isValid() != other.isValid()) return false;
        final Object this$tid = this.getTid();
        final Object other$tid = other.getTid();
        if (!Objects.equals(this$tid, other$tid)) return false;
        final Object this$pid = this.getPid();
        final Object other$pid = other.getPid();
        if (!Objects.equals(this$pid, other$pid)) return false;
        final Object this$userChoice = this.getUserChoice();
        final Object other$userChoice = other.getUserChoice();
        return Objects.equals(this$userChoice, other$userChoice);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TransactionDto;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isValid() ? 79 : 97);
        final Object $tid = this.getTid();
        result = result * PRIME + ($tid == null ? 43 : $tid.hashCode());
        final Object $pid = this.getPid();
        result = result * PRIME + ($pid == null ? 43 : $pid.hashCode());
        final Object $userChoice = this.getUserChoice();
        result = result * PRIME + ($userChoice == null ? 43 : $userChoice.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "TransactionDto(isValid=" + this.isValid() + ", tid=" + this.getTid() + ", pid=" + this.getPid() + ", userChoice=" + this.getUserChoice() + ")";
    }
}
