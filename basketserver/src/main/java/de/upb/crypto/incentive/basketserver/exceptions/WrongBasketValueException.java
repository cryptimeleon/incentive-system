package de.upb.crypto.incentive.basketserver.exceptions;

public class WrongBasketValueException extends BasketServiceException {
    public WrongBasketValueException() {
        super("Basket does not have the same value!");
    }
}
