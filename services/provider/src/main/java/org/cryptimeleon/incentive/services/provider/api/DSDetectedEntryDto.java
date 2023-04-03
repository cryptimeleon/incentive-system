package org.cryptimeleon.incentive.services.provider.api;

import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.math.serialization.converter.JSONConverter;

import java.util.UUID;

@SuppressWarnings("unused")
public class DSDetectedEntryDto {
    private UUID basketId;
    private String userPublicKey;
    private String userInfo;
    private String userSecretExponent;

    public DSDetectedEntryDto() {}
    public DSDetectedEntryDto(UUID basketId, UserInfo userInfo, RegistrationCoupon userRegistrationCoupon) {
        JSONConverter jsonConverter = new JSONConverter();
        this.basketId = basketId;
        this.userInfo = userRegistrationCoupon.getUserInfo();
        this.userPublicKey = jsonConverter.serialize(userInfo.getUpk().getUpk().getRepresentation());
        this.userSecretExponent = jsonConverter.serialize(userInfo.getDsBlame().getRepresentation());
    }

    public UUID getBasketId() {
        return basketId;
    }

    public String getUserPublicKey() {
        return userPublicKey;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public String getUserSecretExponent() {
        return userSecretExponent;
    }
}
