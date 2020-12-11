package de.upb.crypto.incentive.protocols.issue;

import de.upb.crypto.incentive.procotols.issue.IssueHelper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;


public class IssueHelperTest {

    IssueHelper issueHelper = new IssueHelper();

    @Test
    void requestTest() {
        assertEquals(issueHelper.isValidRequest(issueHelper.validRequest), true);
        assertEquals(issueHelper.isValidRequest(issueHelper.invalidRequest), false);
    }

    @Test
    void responseTest() {
        assertEquals(issueHelper.isValidResponse(issueHelper.validRequest, issueHelper.validResponse), true);
        assertEquals(issueHelper.isValidResponse(issueHelper.validRequest, issueHelper.invalidResponse), false);
        assertEquals(issueHelper.isValidResponse(issueHelper.invalidRequest, issueHelper.validResponse), false);
        assertEquals(issueHelper.isValidResponse(issueHelper.invalidRequest, issueHelper.invalidResponse), false);
    }
}
