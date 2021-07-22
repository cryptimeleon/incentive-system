package org.cryptimeleon.incentive.services.basket.exceptions;

public class BasketNotFoundException extends BasketServiceException {
    public BasketNotFoundException() {
        super("Basket not Found");
    }
}
