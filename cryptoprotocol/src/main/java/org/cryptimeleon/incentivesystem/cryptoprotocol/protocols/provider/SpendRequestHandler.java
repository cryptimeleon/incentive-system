package org.cryptimeleon.incentivesystem.cryptoprotocol.protocols.provider;

import org.cryptimeleon.incentivesystem.cryptoprotocol.interfaces.provider.DeductInterface;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;

public class SpendRequestHandler implements DeductInterface, Representable {
    public String computeSerializedDeductReponse(String serializedSpendRequest, String serializedDSID, String serializedTID, long spendAmount) {
        return null;
    }

    public Representation getRepresentation() {
        return null;
    }
}
