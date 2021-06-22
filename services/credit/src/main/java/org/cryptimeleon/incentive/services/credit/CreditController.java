package org.cryptimeleon.incentive.services.credit;

import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.IncentiveClientException;
import org.cryptimeleon.incentive.services.credit.exception.BasketException;
import org.cryptimeleon.incentive.services.credit.exception.IncentiveException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@Slf4j
public class CreditController {

    private CreditService creditService;  // Automatically injects an instance of the service

    /**
     * Endpoint for alive testing etc.
     */
    @GetMapping("/")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Credit Service", HttpStatus.OK);
    }

    /**
     * Run the credit-earn protocol.
     */
    @GetMapping("/credit")
    @ApiOperation(value = "Credit protocol", notes = "Returns a serialized SPSEQ signature.", response = String.class)
    public String credit(
            @RequestHeader(value = "earn-request") String serializedEarnRequest,
            @RequestHeader(value = "basket-id") UUID basketId) throws IncentiveException {
        return creditService.handleEarnRequest(serializedEarnRequest, basketId);
    }

    /**
     * Default error handling for simple control flow.
     */

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(IncentiveException.class)
    public String handleIncentiveException(IncentiveException ex) {
        return "An incentive exception occurred!";
    }

    @ExceptionHandler(IncentiveClientException.class)
    public ResponseEntity<String> handleIncentiveClientException(IncentiveClientException incentiveClientException) {
        return new ResponseEntity<>(incentiveClientException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BasketException.class)
    public ResponseEntity<String> handleBasketException(BasketException basketException) {
        return new ResponseEntity<>(basketException.getMessage(), basketException.getHttpStatus());
    }
}