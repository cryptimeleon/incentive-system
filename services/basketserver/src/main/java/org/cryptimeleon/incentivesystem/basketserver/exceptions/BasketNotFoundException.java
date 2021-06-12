package org.cryptimeleon.incentivesystem.basketserver.exceptions;

public class BasketNotFoundException extends BasketServiceException {
    public BasketNotFoundException() {
        super("Basket not Found");
    }
}
