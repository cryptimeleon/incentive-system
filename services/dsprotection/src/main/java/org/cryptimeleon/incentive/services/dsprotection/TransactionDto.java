package org.cryptimeleon.incentive.services.dsprotection;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.cryptimeleon.incentive.crypto.model.Transaction;

/**
 * A data transfer object representing a transaction as it is treated in the frontend.
 * So a transaction DTO consists of a validity flag, a transaction ID
 * and a string representing the reward that the user wants to claim with this transaction.
 *
 * 
 */
@Data
@AllArgsConstructor
public class TransactionDto {
    private boolean isValid;
    private String tid;
    private String userChoice;

    public TransactionDto(Transaction ta) {
        this.isValid = ta.getIsValid();
        this.tid = ta.getTransactionID().toString();
        this.userChoice = ta.getUserChoice();
    }
}
