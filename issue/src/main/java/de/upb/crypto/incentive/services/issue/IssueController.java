package de.upb.crypto.incentive.services.issue;

import de.upb.crypto.incentive.protocols.issue.IssueRequest;
import de.upb.crypto.incentive.protocols.issue.IssueResponse;
import de.upb.crypto.incentive.protocols.model.Token;
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
    public ResponseEntity<IssueResponse> greeting(@Validated IssueRequest request) {
        return new ResponseEntity<>(new IssueResponse(request.getId(), new Token(0)), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(IncentiveException.class)
    public String handleIncentiveException(IncentiveException ex) {
        return "An incentive exception occurred!";
    }
}