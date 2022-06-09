package org.cryptimeleon.incentive.services.promotion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BasketNotPaidException extends RuntimeException {

    public BasketNotPaidException() {
        super("Basket must be paid to retrieve the results of Earn/Spend!");
    }
}
