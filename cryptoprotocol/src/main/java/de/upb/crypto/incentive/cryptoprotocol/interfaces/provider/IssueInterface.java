package de.upb.crypto.incentive.cryptoprotocol.interfaces.provider;

public interface IssueInterface {
    // Provider is stateless (besides pp, pk, sk) => provide all information of request via parameters
    // pp, pk, sk provided via constructor of the class implementing this interface

    String computeSerializedIssueReponse(String serializedJoinRequest, String serializedUserPublicKey);
}
