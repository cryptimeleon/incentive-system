package org.cryptimeleon.incentivesystem.cryptoprotocol.protocols.provider;

import org.cryptimeleon.incentivesystem.cryptoprotocol.interfaces.provider.IssueInterface;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;

public class JoinRequestHandler implements IssueInterface, Representable {
    public String computeSerializedIssueReponse(String serializedJoinRequest, String serializedUserPublicKey) {
        return null;
    }

    public Representation getRepresentation() {
        return null;
    }
}
