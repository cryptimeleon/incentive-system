package org.cryptimeleon.incentivesystem.basket.exceptions;

public class WrongBasketValueException extends BasketServiceException {
    public WrongBasketValueException() {
        super("Basket does not have the same value!");
    }
}
