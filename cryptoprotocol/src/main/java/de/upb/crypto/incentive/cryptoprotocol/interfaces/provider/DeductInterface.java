package de.upb.crypto.incentive.cryptoprotocol.interfaces.provider;

public interface DeductInterface {
  // pp, pk, sk are provided via constructor of the class implementing this
  // interface

  // spendAmount should be verified
  String computeSerializedDeductReponse(
      String serializedSpendRequest, String serializedDSID, String serializedTID, long spendAmount);
}
