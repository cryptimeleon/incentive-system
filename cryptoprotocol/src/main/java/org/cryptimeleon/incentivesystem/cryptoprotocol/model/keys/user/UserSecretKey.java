package org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user;

import org.cryptimeleon.craco.prf.PrfKey;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

public class UserSecretKey {
    private ZnElement usk;
    private PrfKey prfUserKey; // user's key for generating pseudorandomness using the PRF

    public UserSecretKey(ZnElement usk, PrfKey prfUserKey) {
        this.usk = usk;
        this.prfUserKey = prfUserKey;
    }
}
