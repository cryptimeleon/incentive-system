package de.upb.crypto.incentive.cryptoprotocol.model.keys.provider;

import de.upb.crypto.craco.sig.sps.eq.SPSEQVerificationKey;
import de.upb.crypto.math.structures.groups.cartesian.GroupElementVector;

import java.util.Arrays;

public class ProviderPublicKey {
    private SPSEQVerificationKey pkSpsEq;
    private GroupElementVector h; // first six bases for the Pedersen commitment in the tokens

    public ProviderPublicKey(SPSEQVerificationKey pkSpsEq, GroupElementVector h) throws IllegalArgumentException {
        // asserting correct number of group elements passed
        if (h.length() != 6) {
            throw new IllegalArgumentException("h is required to consist of 6 group elements, found: " + String.valueOf(h.length()));
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
