package org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token;
import org.cryptimeleon.math.structures.groups.GroupElement;

/**
 * a class representing the final output of the Join algorithm in the Issue <-> Join protocol.
 */
@Value
@AllArgsConstructor
public class JoinOutput
{
    private Token token; // empty token created by executing Issue <-> Join
    private GroupElement dsid; // double-spending ID for the created token
}
