package org.cryptimeleon.incentive.crypto;

import lombok.AllArgsConstructor;
import org.cryptimeleon.math.structures.rings.zn.Zn;

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
}
