package org.cryptimeleon.incentivesystem.basket.exceptions;

public class ItemNotFoundException extends BasketServiceException {
    public ItemNotFoundException() {
        super("Item not Found");
    }
}
