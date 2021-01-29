package de.upb.crypto.incentive.cryptoprotocol.protocols.provider;

import de.upb.crypto.incentive.cryptoprotocol.interfaces.provider.IssueInterface;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;

public class JoinRequestHandler implements IssueInterface, Representable
{
    public String computeSerializedIssueReponse(String serializedJoinRequest, String serializedUserPublicKey)
    {
        return null;
    }

    public Representation getRepresentation()
    {
        return null;
    }
}
