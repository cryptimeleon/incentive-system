package de.upb.crypto.incentive.cryptoprotocol.protocols.provider;

import de.upb.crypto.incentive.cryptoprotocol.interfaces.provider.DeductInterface;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;

public class SpendRequestHandler implements DeductInterface, Representable {
    public String computeSerializedDeductReponse(String serializedSpendRequest, String serializedDSID, String serializedTID, long spendAmount) {
        return null;
    }

    public Representation getRepresentation() {
        return null;
    }
}
