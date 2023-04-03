package org.cryptimeleon.incentive.services.store.exceptions;

public class BasketNotFoundException extends BasketServiceException {
    public BasketNotFoundException() {
        super("Basket not Found");
    }
}
