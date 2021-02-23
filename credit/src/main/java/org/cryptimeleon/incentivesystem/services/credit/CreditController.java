package org.cryptimeleon.incentivesystem.services.credit;

import org.cryptimeleon.incentivesystem.protocoldefinition.creditearn.EarnRequest;
import org.cryptimeleon.incentivesystem.protocoldefinition.creditearn.CreditResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CreditController {

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.debug("Called test function");
        return new ResponseEntity<>("Its working", HttpStatus.OK);
    }

    @GetMapping("/credit")
    @ApiOperation(value = "Credit protocol", notes = "Earn to a token.", response = CreditResponse.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = CreditResponse.class),
            @ApiResponse(code = 403, message = "Invalid Credit Request", response = String.class)
    })
    public ResponseEntity<CreditResponse> greeting(@Validated EarnRequest request) throws IncentiveException {
        if (request.getEarnAmount() < 0) {
            throw new IncentiveException();
        }
        // TODO query basket server to check if request is valid
        return new ResponseEntity<>(new CreditResponse(request.getId(), "Test credit response") , HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(IncentiveException.class)
    public String handleIncentiveException(IncentiveException ex) {
        return "An incentive exception occurred!";
    }
}