package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.services.basket.repository.CryptoRepository;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreService {

    private final CryptoRepository cryptoRepository;

    @Autowired
    private StoreService(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    public String registerUserAndReturnSerializedRegistrationCoupon(String serializedUserPublicKey, String userInfo) {
        JSONConverter jsonConverter = new JSONConverter();
        UserPublicKey userPublicKey = new UserPublicKey(jsonConverter.deserialize(serializedUserPublicKey), cryptoRepository.getPublicParameters());
        RegistrationCoupon registrationCoupon = cryptoRepository.getIncentiveSystem().signRegistrationCoupon(cryptoRepository.getStoreKeyPair(), userPublicKey, userInfo);
        return jsonConverter.serialize(registrationCoupon.getRepresentation());
    }
}
