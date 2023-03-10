package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RegistrationCouponRepository {
    private final List<RegistrationCoupon> registrationCouponList = new ArrayList<>();

    public void addCoupon(RegistrationCoupon registrationCoupon) {
        registrationCouponList.add(registrationCoupon);
    }

    public List<RegistrationCoupon> getAllCoupons() {
        return registrationCouponList;
    }

    public Optional<RegistrationCoupon> findEntryFor(UserPublicKey userPublicKey) {
        return registrationCouponList.stream()
                .filter(registrationCoupon -> registrationCoupon.getUserPublicKey().equals(userPublicKey))
                .findAny();
    }
}
