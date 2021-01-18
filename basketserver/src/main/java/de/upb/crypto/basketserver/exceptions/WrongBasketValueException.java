package de.upb.crypto.basketserver.exceptions;

public class WrongBasketValueException extends BasketServiceException {
    public WrongBasketValueException() {
        super("Basket does not have the same value!");
    }
}
