package org.cryptimeleon.incentive.crypto.exception;

public class StoreDoubleSpendingDetectedException extends Exception {
    public StoreDoubleSpendingDetectedException(String message) {
        super(message);
    }
}
