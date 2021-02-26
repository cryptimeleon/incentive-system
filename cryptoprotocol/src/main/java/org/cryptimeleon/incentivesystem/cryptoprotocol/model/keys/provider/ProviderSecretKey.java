package org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider;

import org.cryptimeleon.craco.prf.PrfKey;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSigningKey;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;

public class ProviderSecretKey {
    private SPSEQSigningKey skSpsEq;
    private RingElementVector q; // dlogs of the first six bases used in the Pedersen commitment in the token
    private PrfKey betaProv;

    public ProviderSecretKey(SPSEQSigningKey skSpsEq, RingElementVector q, PrfKey betaProv) throws IllegalArgumentException {
        // assert that correct number of Zn exponents is passed ()
        if (q.length() != 6) {
            throw new IllegalArgumentException("q is required to consist of 6 group elements, found: " + q.length());
        }

        this.skSpsEq = skSpsEq;

        this.q = q;

        this.betaProv = betaProv;
    }

    public SPSEQSigningKey getSkSpsEq() {
        return skSpsEq;
    }

    public RingElementVector getQ() {
        return this.q;
    }
}
