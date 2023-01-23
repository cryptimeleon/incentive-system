package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;

/**
 * Interface to support a lambda function as a callback to add the users registration coupon to some database or store.
 */
public interface IRegistrationCouponDBHandler {
    void storeUserData(RegistrationCoupon registrationCoupon);
}
