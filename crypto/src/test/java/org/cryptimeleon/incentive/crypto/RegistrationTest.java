package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.callback.IRegistrationCouponDBHandler;
import org.cryptimeleon.incentive.crypto.callback.IStorePublicKeyVerificationHandler;
import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.store.StoreKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Performs a full example run of all three protocols as in a real-world setting.
 * A new user joins the system using the Issue-Join protocol,
 * then earns some points using the Credit-Earn protocol
 * and performs some (valid as well as invalid) Spend operations.
 * Note: since no state is stored on either side at cryptoprotocol level, it makes no sense to test with multiple users here.
 */
public class RegistrationTest {

    IncentiveSystem incSys = TestSuite.incentiveSystem;
    StoreKeyPair skp = TestSuite.storeKeyPair;
    ProviderKeyPair pkp = TestSuite.providerKeyPair;
    UserKeyPair ukp = TestSuite.userKeyPair;

    @Test
    public void registrationTest() {
        UserPublicKey upk = ukp.getPk();
        String userInfo = "Name: Max Mustermann, ID-Number: 12345678";
        List<RegistrationCoupon> registrationCouponList = new ArrayList<>();
        IRegistrationCouponDBHandler iRegistrationCouponDBHandler = registrationCouponList::add;
        IStorePublicKeyVerificationHandler iStorePublicKeyVerificationHandler = (s) -> true;


        RegistrationCoupon registrationCoupon = incSys.signRegistrationCoupon(skp, upk, userInfo);
        SPSEQSignature registrationToken = incSys.verifyRegistrationCouponAndIssueRegistrationToken(
                pkp,
                registrationCoupon,
                iStorePublicKeyVerificationHandler,
                iRegistrationCouponDBHandler
        );

        assertThat(incSys.verifyRegistrationCoupon(registrationCoupon, (s) -> true)).isTrue();
        assertThat(registrationCouponList).hasSize(1);
        assertThat(incSys.verifyRegistrationToken(pkp.getPk(), registrationToken, registrationCoupon)).isTrue();

        // Some additional representation test here to avoid duplicate setup
        RegistrationCoupon deserializedRegistrationCoupon = new RegistrationCoupon(registrationCoupon.getRepresentation(), TestSuite.incentiveSystemRestorer);
        assertThat(registrationCoupon).isEqualTo(deserializedRegistrationCoupon);
    }
}
