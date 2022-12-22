package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Data class for user randomness used in issue-join protocol.
 */
public class IssueJoinRandomness {
    final Zn.ZnElement eskUsr;
    final Zn.ZnElement dsrnd0;
    final Zn.ZnElement dsrnd1;
    final Zn.ZnElement z;
    final Zn.ZnElement t;
    final Zn.ZnElement u;
    final Zn.ZnElement blindGenesisR;

    public IssueJoinRandomness(Zn.ZnElement eskUsr, Zn.ZnElement dsrnd0, Zn.ZnElement dsrnd1, Zn.ZnElement z, Zn.ZnElement t, Zn.ZnElement u, Zn.ZnElement blindGenesisR) {
        this.eskUsr = eskUsr;
        this.dsrnd0 = dsrnd0;
        this.dsrnd1 = dsrnd1;
        this.z = z;
        this.t = t;
        this.u = u;
        this.blindGenesisR = blindGenesisR;
    }

    static IssueJoinRandomness generate(IncentivePublicParameters pp) {
        Vector<Zn.ZnElement> rv = pp.getBg().getZn().getUniformlyRandomElements(7).map(Zn.ZnElement.class::cast);
        return new IssueJoinRandomness(rv.get(0), rv.get(1), rv.get(2), rv.get(3), rv.get(4), rv.get(5), rv.get(6));
    }
}
