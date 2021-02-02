package de.upb.crypto.incentive.cryptoprotocol.model.messages;

import de.upb.crypto.craco.sig.sps.eq.SPSEQSignature;
import de.upb.crypto.math.interfaces.structures.GroupElement;

public class EarnRequestMessage
{
    private GroupElement blindedCommitment;
    private SPSEQSignature blindedCertificate;
}
