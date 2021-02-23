package org.cryptimeleon.incentivesystem.cryptoprotocol.interfaces.user;

public interface EarnInterface {
    // One instance of the implementing class per request
    // pp, pk, USK, k, token are provided via constructor of class that implements this interface

    // How to store randomness s: One instance per request and identify it via uuids?

    String generateSerializedEarnRequest();

    // Assume that randomness s is stored in a variable of this instance
    String handleSerializedCreditResponse(String serializedCreditResponse);
    // TODO discuss output and implicit update of values
}
