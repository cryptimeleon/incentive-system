package org.cryptimeleon.incentivesystem.services.issue;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.cryptimeleon.incentivesystem.services.issue.model.JoinRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class IssueController {

    private IssueService issueService;

    /*
     * Endpoint for alive testing etc.
     */
    @GetMapping("/")
    public ResponseEntity<String> test() {
        issueService.init();
        return new ResponseEntity<>("Issue Service", HttpStatus.OK);
    }

    @GetMapping("/issue")
    @ApiOperation(value = "Issuing protocol", notes = "Issue a new incentive token.", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 403, message = "Invalid Issuing Request", response = String.class)
    })
    public ResponseEntity<String> performIssueJoin(@Validated JoinRequestDTO joinRequestDTO) {
        return new ResponseEntity<>(issueService.runIssueJoinProtocol(joinRequestDTO), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(IncentiveException.class)
    public String handleIncentiveException(IncentiveException ex) {
        return "An incentive exception occurred!";
    }
}