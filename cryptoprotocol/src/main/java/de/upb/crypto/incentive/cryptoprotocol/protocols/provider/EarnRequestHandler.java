package de.upb.crypto.incentive.cryptoprotocol.protocols.provider;

import de.upb.crypto.incentive.cryptoprotocol.interfaces.provider.CreditInterface;

/**
 * A handler class implementing the provider side of the Credit <-> Earn protocol (i.e. the Credit algorithm).
 * Handles Earn requests made by users.
 */
public class EarnRequestHandler implements CreditInterface
{
    public String computeSerializedResponse(String serializedEarnRequest, long earnAmount)
    {
        // deserialize earn request


        // update commitment

        // certify updated commitment

        // serialize response
        return null;
    }

}
