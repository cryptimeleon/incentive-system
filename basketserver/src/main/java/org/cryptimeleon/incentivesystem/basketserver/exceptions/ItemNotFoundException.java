package org.cryptimeleon.incentivesystem.basketserver.exceptions;

public class ItemNotFoundException extends BasketServiceException {
    public ItemNotFoundException() {
        super("Item not Found");
    }
}
