package de.upb.crypto.incentive.cryptoprotocol.protocols.provider;

import de.upb.crypto.incentive.cryptoprotocol.interfaces.provider.CreditInterface;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;

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
