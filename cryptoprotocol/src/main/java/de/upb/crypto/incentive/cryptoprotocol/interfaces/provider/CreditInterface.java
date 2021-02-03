package de.upb.crypto.incentive.cryptoprotocol.interfaces.provider;

public interface CreditInterface {
    // pp, pk, sk are provided via constructor of the class implementing this interface

    // earnAmount (k) can be assumed to be verified by credit service (using basket server)
    String computeSerializedResponse(String serializedEarnRequest, long earnAmount);
}
