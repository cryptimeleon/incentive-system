package de.upb.crypto.incentive.services.credit;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IncentiveException extends Exception {
    IncentiveException(String msg) {
        super(msg);
    }

}
