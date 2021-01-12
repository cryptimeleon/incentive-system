package de.upb.crypto.incentive.cryptoprotocol.interfaces.user;

public interface SpendInterface {
    // One instance of the implementing class per request
    // pp, pk, k, dsid, tid, USK, token are provided through the constructor of the implementing class

    // We store generated variables as object variables:
    // i.e. r_i,
    // And parsed values / pseudorandom exponents
    // e.g. esk / esk*_usr

    String generateSerializedSpendRequest();

    // Takes serialized response and outputs serialized Token
    String handleSerializedDeductResponse(String serializedDeductResponse);
    // TODO discuss output format, eventually second version that returns data object
}
