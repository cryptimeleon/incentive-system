package de.upb.crypto.incentive.procotols.issue;

public class IssueHelper {
    final public IssueRequest validRequest = new IssueRequest(0, "Valid Request");
    final public IssueRequest invalidRequest = new IssueRequest(0, "Invalid Request");
    final public IssueResponse validResponse = new IssueResponse(0, "Valid Response");
    final public IssueResponse invalidResponse = new IssueResponse(0, "Invalid Response");

    public boolean isValidRequest(IssueRequest request) {
        return request.equals(validRequest);
    }

    public boolean isValidResponse(IssueRequest request, IssueResponse response) {
        return request.equals(validRequest) && response.equals(validResponse);
    }
}
