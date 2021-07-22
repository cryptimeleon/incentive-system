package org.cryptimeleon.incentive.services.issue;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class IssueController {

    private IssueService issueService;

    /**
     * Endpoint for alive testing etc.
     */
    @GetMapping("/")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Hello from Issue service!", HttpStatus.OK);
    }

    @GetMapping("/issue")
    @ApiOperation(value = "Issuing protocol", notes = "Issue a new incentive token.", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 403, message = "Invalid Issuing Request", response = String.class)
    })
    public ResponseEntity<String> performIssueJoin(
            @RequestHeader(value = "join-request") String serializedJoinRequest,
            @RequestHeader(value = "public-key") String serializedUserPublicKey
    ) {
        return new ResponseEntity<>(issueService.runIssueJoinProtocol(serializedJoinRequest, serializedUserPublicKey), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(RuntimeException.class)
    public String handleIncentiveException(RuntimeException ex) {
        return "An exception occurred!";
    }
}