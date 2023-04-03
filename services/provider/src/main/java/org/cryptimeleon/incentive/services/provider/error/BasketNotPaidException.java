package org.cryptimeleon.incentive.services.provider.error;

public class BasketNotPaidException extends IncentiveServiceException {
    public BasketNotPaidException() {
        super("Basket must be paid to retrieve the results of Earn/Spend!");
    }
}
