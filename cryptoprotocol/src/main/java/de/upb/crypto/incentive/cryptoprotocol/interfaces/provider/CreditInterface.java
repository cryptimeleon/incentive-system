package de.upb.crypto.incentive.cryptoprotocol.interfaces.provider;

public interface CreditInterface {
    // pp, pk, sk are provided via constructor of the class implementing this interface

    // earnAmount (k) should be trusted / verified
    String computeSerializedReponse(String serializedEarnRequest, long earnAmount);
}
