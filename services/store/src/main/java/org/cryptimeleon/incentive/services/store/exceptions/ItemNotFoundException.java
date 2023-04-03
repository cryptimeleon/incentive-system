package org.cryptimeleon.incentive.services.store.exceptions;

public class ItemNotFoundException extends BasketServiceException {
    public ItemNotFoundException() {
        super("Item not Found");
    }
}
