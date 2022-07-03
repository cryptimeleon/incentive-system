package org.cryptimeleon.incentivesystem.dsprotectionservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.cryptimeleon.incentive.crypto.model.Transaction;

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
