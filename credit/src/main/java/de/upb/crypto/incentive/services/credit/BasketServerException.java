package de.upb.crypto.incentive.services.credit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@Setter
public class BasketServerException extends IncentiveException {
    private String errorMessage;
    private HttpStatus httpStatus;
}
