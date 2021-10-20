package org.cryptimeleon.incentive.crypto.proof;

import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.craco.protocols.arguments.sigma.SigmaProtocol;

public abstract class SpendDeductLeafNode extends SpendDeductTree {

    public abstract SigmaProtocol getProtocol();

    public abstract boolean isTrue();

    public abstract String getLeafName();

    public abstract SecretInput getWitness();
}
