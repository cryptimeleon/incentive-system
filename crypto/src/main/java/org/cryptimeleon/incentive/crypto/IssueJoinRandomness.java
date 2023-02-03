package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Data class for user randomness used in issue-join protocol.
 */
public class IssueJoinRandomness {
    final Zn.ZnElement dsidUser;
    final Zn.ZnElement dsrnd;
    final Zn.ZnElement z;
    final Zn.ZnElement t;
    final Zn.ZnElement u;
    final Zn.ZnElement blindRegistrationSignatureR;

    public IssueJoinRandomness(Zn.ZnElement dsidUsr, Zn.ZnElement dsrnd, Zn.ZnElement z, Zn.ZnElement t, Zn.ZnElement u, Zn.ZnElement blindRegistrationSignatureR) {
        this.dsidUser = dsidUsr;
        this.dsrnd = dsrnd;
        this.z = z;
        this.t = t;
        this.u = u;
        this.blindRegistrationSignatureR = blindRegistrationSignatureR;
    }

    static IssueJoinRandomness generate(IncentivePublicParameters pp) {
        Vector<Zn.ZnElement> rv = pp.getBg().getZn().getUniformlyRandomElements(6).map(Zn.ZnElement.class::cast);
        return new IssueJoinRandomness(rv.get(0), rv.get(1), rv.get(2), rv.get(3), rv.get(4), rv.get(5));
    }
}
