package org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.math.structures.groups.GroupElement;

public class EarnRequestMessage {
    private GroupElement blindedCommitment;
    private SPSEQSignature blindedCertificate;
}
