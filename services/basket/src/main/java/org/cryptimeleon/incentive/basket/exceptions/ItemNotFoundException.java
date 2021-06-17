package org.cryptimeleon.incentive.basket.exceptions;

public class ItemNotFoundException extends BasketServiceException {
    public ItemNotFoundException() {
        super("Item not Found");
    }
}
