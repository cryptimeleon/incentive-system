package org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/**
 * A class representing the third message of the Issue <-> Join protocol.
 */
@Value
@AllArgsConstructor
public class JoinResponse
{
    private SPSEQSignature preCertificate; // preliminary certificate for the user token
    private ZnElement eskProv; // the share of the provider for the ElGamal encryption key for the initial token of the user
}
