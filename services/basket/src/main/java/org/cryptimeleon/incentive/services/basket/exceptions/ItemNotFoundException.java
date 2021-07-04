package org.cryptimeleon.incentive.services.basket.exceptions;

public class ItemNotFoundException extends BasketServiceException {
    public ItemNotFoundException() {
        super("Item not Found");
    }
}
