package org.cryptimeleon.incentive.services.incentive.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code=HttpStatus.BAD_REQUEST, reason = "Basket already payed and thus cannot be used for promotions!")
public class BasketAlreadyPayedException extends IncentiveServiceException {
    public BasketAlreadyPayedException() {
        super("");
    }
}
