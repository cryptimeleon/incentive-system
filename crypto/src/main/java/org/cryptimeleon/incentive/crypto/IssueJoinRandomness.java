package org.cryptimeleon.incentive.crypto;

import lombok.AllArgsConstructor;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.stream.Stream;

/**
 * Data class for user randomness used in issue-join protocol.
 */
@AllArgsConstructor
public class IssueJoinRandomness {
    final Zn.ZnElement eskUsr;
    final Zn.ZnElement dsrnd0;
    final Zn.ZnElement dsrnd1;
    final Zn.ZnElement z;
    final Zn.ZnElement t;
    final Zn.ZnElement u;
    final Zn.ZnElement blindGenesisR;

    static IssueJoinRandomness generate(IncentivePublicParameters pp) {
        Zn.ZnElement[] rv = Stream.generate(pp.getBg().getZn()::getUniformlyRandomElement).limit(7).toArray(Zn.ZnElement[]::new);
        return new IssueJoinRandomness(rv[0], rv[1], rv[2], rv[3], rv[4], rv[5], rv[6]);
    }
}
