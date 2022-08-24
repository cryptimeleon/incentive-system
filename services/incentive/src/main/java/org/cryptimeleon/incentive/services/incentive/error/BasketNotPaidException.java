package org.cryptimeleon.incentive.services.incentive.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Cannot retrieve token update results! Basket must be payed!")
public class BasketNotPaidException extends IncentiveServiceException {
    public BasketNotPaidException() {
        super("Basket must be paid to retrieve the results of Earn/Spend!");
    }
}
