package de.upb.crypto.incentive.basketserver.exceptions;

public class BasketNotFoundException extends BasketServiceException {
  public BasketNotFoundException() {
    super("Basket not Found");
  }
}
