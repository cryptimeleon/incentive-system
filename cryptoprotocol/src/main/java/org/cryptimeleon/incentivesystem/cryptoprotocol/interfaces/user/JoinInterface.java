package org.cryptimeleon.incentivesystem.cryptoprotocol.interfaces.user;

public interface JoinInterface {
    // One instance of the implementing class per request
    // pp, pk, upk, USK are provided through the constructor of the implementing class

    // We store generated variables as object variables:
    // i.e. esk*_usr, dsrnd*_0, dsrnd*_1, z*, t*, u*, C^(pre)

    // Generate C^(pre), returns serialized non-interactive proof and request data
    String generateSerializedJoinRequest();

    // Takes serialized response and outputs serialized Token
    String handleSerializedIssueResponse(String serializedIssueResponse);
    // TODO discuss output and implicit update of values
    // TODO provide token and amount via constructor?
}