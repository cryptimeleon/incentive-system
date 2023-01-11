package org.cryptimeleon.incentive.services.incentive;

import org.cryptimeleon.incentive.services.incentive.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreService {
    private final CryptoRepository cryptoRepository;

    @Autowired
    private StoreService(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }
}
