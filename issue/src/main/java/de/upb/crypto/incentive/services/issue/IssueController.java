package de.upb.crypto.incentive.services.issue;

import de.upb.crypto.incentive.procotols.issue.IssueHelper;
import de.upb.crypto.incentive.procotols.issue.IssueRequest;
import de.upb.crypto.incentive.procotols.issue.IssueResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
public class IssueController {

    IssueHelper issueHelper = new IssueHelper();

    @GetMapping("/issue")
    @ApiOperation(value = "Issuing protocol", notes = "Issue a new incentive token.", response = IssueResponse.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = IssueResponse.class),
            @ApiResponse(code = 403, message = "Invalid Issuing Request", response = String.class)
    })
    public ResponseEntity<IssueResponse> greeting(@Validated @ModelAttribute IssueRequest request) throws IncentiveException {
        if (request.equals(issueHelper.validRequest)) {
            return new ResponseEntity<>(issueHelper.validResponse, HttpStatus.OK);
        }
        throw new IncentiveException();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(IncentiveException.class)
    public String handleIncentiveException(IncentiveException ex) {
        return "An incentive exception occurred!";
    }
}