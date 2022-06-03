package org.cryptimeleon.incentive.services.promotion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BasketNotPayedException extends RuntimeException {

    public BasketNotPayedException() {
        super("Basket must be payed to retrieve the results of Earn/Spend!");
    }
}
