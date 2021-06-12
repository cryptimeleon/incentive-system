package org.cryptimeleon.incentivesystem.services.credit;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IncentiveException extends Exception {
    IncentiveException(String msg) {
        super(msg);
    }

}
