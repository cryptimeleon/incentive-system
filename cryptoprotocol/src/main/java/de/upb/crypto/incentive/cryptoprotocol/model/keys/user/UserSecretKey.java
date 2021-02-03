package de.upb.crypto.incentive.cryptoprotocol.model.keys.user;

import de.upb.crypto.craco.prf.PrfKey;
import de.upb.crypto.math.structures.rings.zn.Zn.ZnElement;

public class UserSecretKey {
    private ZnElement usk;
    private PrfKey prfUserKey; // user's key for generating pseudorandomness using the PRF

    public UserSecretKey(ZnElement usk, PrfKey prfUserKey) {
        this.usk = usk;
        this.prfUserKey = prfUserKey;
    }
}
