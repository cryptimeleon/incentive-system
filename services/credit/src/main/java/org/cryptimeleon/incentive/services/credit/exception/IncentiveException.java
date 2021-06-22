package org.cryptimeleon.incentive.services.credit.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IncentiveException extends RuntimeException {
    public IncentiveException(String msg) {
        super(msg);
    }
}
