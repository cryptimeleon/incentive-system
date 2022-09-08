package org.cryptimeleon.incentive.services.incentive;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown in incentive controller when user attempts to query token updates (earned points + granted rewards) for a basket that is not paid.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BasketNotPaidException extends RuntimeException {

    public BasketNotPaidException() {
        super("Basket must be paid to retrieve the results of Earn/Spend!");
    }
}
