package org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQVerificationKey;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;

import java.util.Arrays;

public class ProviderPublicKey {
    private final SPSEQVerificationKey pkSpsEq;
    private final GroupElementVector h; // first six bases for the Pedersen commitment in the tokens

    public ProviderPublicKey(SPSEQVerificationKey pkSpsEq, GroupElementVector h) throws IllegalArgumentException {
        // asserting correct number of group elements passed
        if (h.length() != 6) {
            throw new IllegalArgumentException("h is required to consist of 6 group elements, found: " + h.length());
        }

        this.pkSpsEq = pkSpsEq;

        this.h = h;
    }

    public SPSEQVerificationKey getPkSpsEq() {
        return this.pkSpsEq;
    }

    public GroupElementVector getH() {
        return h;
    }
}
