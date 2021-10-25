package org.cryptimeleon.incentive.services.deduct;

import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
public class DeductController {
    private DeductService deductService; // instance of the web service

    /**
     * Endpoint for heartbeat checks.
     * @return hard-coded response
     */
    @GetMapping("/")
    public ResponseEntity<String> heartbeat(){
        return new ResponseEntity<String>("Hello from Deduct service!", HttpStatus.OK);
    }

    /**
     * Runs the Deduct algorithm and adds transaction data to the database.
     * @param serializedSpendRequest the spend request to handle using the Deduct algorithm.
     * @param basketID identifier for user basket
     * @return serialized spend request
     */
    @PostMapping("/deduct")
    @ApiOperation(value = "Deduct algorithm from the Spend-Deduct protocol.", notes = "Returns a serialized spend response.", response = String.class)
    public String runDeduct(
            @RequestBody String serializedSpendRequest,
            @RequestHeader(value = "basket-id") UUID basketID) {
        return deductService.runDeduct(serializedSpendRequest, basketID);
    }

    /**
     * Returns a 403 Response whenever a runtime exception occurs somewhere in the DeductController class (automatic exception handling, simplifies control flow in methods).
     * @param ex runtime exception to handle
     * @return message of the exception that occured
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex) {
        System.out.println("handling runtime exception");
        System.out.println("message is: " + ex.getMessage());
        return ex.getMessage();
    }
}
