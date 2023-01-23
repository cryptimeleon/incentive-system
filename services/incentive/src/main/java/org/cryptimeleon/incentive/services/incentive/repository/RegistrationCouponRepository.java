package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RegistrationCouponRepository {
    private final List<RegistrationCoupon> registrationCouponList = new ArrayList<>();

    public void addCoupon(RegistrationCoupon registrationCoupon) {
        registrationCouponList.add(registrationCoupon);
    }

    public List<RegistrationCoupon> getAllCoupons() {
        return registrationCouponList;
    }
}
