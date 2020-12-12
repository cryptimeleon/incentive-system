package de.upb.crypto.incentive.services.issue;

import de.upb.crypto.incentive.procotols.issue.IssueHelper;
import de.upb.crypto.incentive.procotols.issue.IssueRequest;
import de.upb.crypto.incentive.procotols.issue.IssueResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IssueController {

    IssueHelper issueHelper = new IssueHelper();

    @GetMapping("/issue")
    public IssueResponse greeting(@Validated @ModelAttribute IssueRequest request) {
        if (request.equals(issueHelper.validRequest)) {
            return issueHelper.validResponse;
        }
        return null;
    }
}