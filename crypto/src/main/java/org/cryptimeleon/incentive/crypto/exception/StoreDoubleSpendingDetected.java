package org.cryptimeleon.incentive.crypto.exception;

public class StoreDoubleSpendingDetected extends Exception {
    public StoreDoubleSpendingDetected(String message) {
        super(message);
    }
}
