package de.upb.crypto.incentive.cryptoprotocol.model.keys.user;

import org.cryptimeleon.craco.prf.PrfKey;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;
import lombok.Data;

@Data
public class UserSecretKey {
    private ZnElement usk;
    private PrfKey prfUserKey; // user's key for generating pseudorandomness using the PRF

    public UserSecretKey(ZnElement usk, PrfKey prfUserKey) {
        this.usk = usk;
        this.prfUserKey = prfUserKey;
    }
}
