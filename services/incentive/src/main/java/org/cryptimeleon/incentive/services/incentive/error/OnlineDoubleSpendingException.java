package org.cryptimeleon.incentive.services.incentive.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class OnlineDoubleSpendingException extends IncentiveServiceException {
    public OnlineDoubleSpendingException() {
        super("");
    }
}
