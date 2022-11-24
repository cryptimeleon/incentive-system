package org.cryptimeleon.incentive.services.dsprotection;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.cryptimeleon.incentive.crypto.model.Transaction;

/**
 * A data transfer object representing a transaction as it is treated in the frontend.
 * So a transaction DTO consists of a validity flag, a transaction ID
 * and a string representing the reward that the user wants to claim with this transaction.
 *
 * We decided to not display the double-spending tag in the frontend to avoid obfuscation.
 */
@Data
@AllArgsConstructor
public class TransactionDto {
    private boolean isValid; // validity state of this transaction
    private String tid; // transaction ID
    private String pid; // promotion ID of the promotion that the user takes part in
    private String userChoice; // string representing the reward that the user wanted to claim with this transaction

    public TransactionDto(Transaction ta) {
        this.isValid = ta.getIsValid();
        this.tid = ta.getTransactionID().toString();
        this.pid = ta.getPromotionId().toString();
        this.userChoice = ta.getUserChoice();
    }
}
