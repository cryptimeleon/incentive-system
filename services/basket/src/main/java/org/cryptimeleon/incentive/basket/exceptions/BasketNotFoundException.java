package org.cryptimeleon.incentive.basket.exceptions;

public class BasketNotFoundException extends BasketServiceException {
    public BasketNotFoundException() {
        super("Basket not Found");
    }
}
