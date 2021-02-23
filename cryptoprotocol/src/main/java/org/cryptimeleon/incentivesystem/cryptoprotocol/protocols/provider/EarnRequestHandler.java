package org.cryptimeleon.incentivesystem.cryptoprotocol.protocols.provider;

import org.cryptimeleon.incentivesystem.cryptoprotocol.interfaces.provider.CreditInterface;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;

/**
 * A handler class implementing the provider side of the Credit <-> Earn protocol (i.e. the Credit algorithm).
 * Handles Earn requests made by users.
 */
public class EarnRequestHandler implements CreditInterface, Representable {
    public String computeSerializedResponse(String serializedEarnRequest, long earnAmount) {
        // deserialize earn request


        // update commitment

        // certify updated commitment

        // serialize response
        return null;
    }

    public Representation getRepresentation() {
        return null;
    }
}
