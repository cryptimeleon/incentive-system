package org.cryptimeleon.incentive.services.incentive.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.I_AM_A_TEAPOT, reason = "Double-spending attempt detected and prevented!")
public class OnlineDoubleSpendingException extends IncentiveServiceException {
    public OnlineDoubleSpendingException() {
        super("");
    }
}
