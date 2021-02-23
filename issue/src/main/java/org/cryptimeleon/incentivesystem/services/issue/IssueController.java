package org.cryptimeleon.incentivesystem.services.issue;

import org.cryptimeleon.incentivesystem.protocoldefinition.issuejoin.IssueResponse;
import org.cryptimeleon.incentivesystem.protocoldefinition.issuejoin.JoinRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
public class IssueController {

    @GetMapping("/issue")
    @ApiOperation(value = "Issuing protocol", notes = "Issue a new incentive token.", response = IssueResponse.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = IssueResponse.class),
            @ApiResponse(code = 403, message = "Invalid Issuing Request", response = String.class)
    })
    public ResponseEntity<IssueResponse> greeting(@Validated JoinRequest request) {
        return new ResponseEntity<>(new IssueResponse(request.getId(), "Some serialized response"), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(IncentiveException.class)
    public String handleIncentiveException(IncentiveException ex) {
        return "An incentive exception occurred!";
    }
}