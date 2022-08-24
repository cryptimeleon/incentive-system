package org.cryptimeleon.incentive.services.incentive.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class BasketNotPaidException extends IncentiveServiceException {
    public BasketNotPaidException() {
        super("Basket must be paid to retrieve the results of Earn/Spend!");
    }
}
