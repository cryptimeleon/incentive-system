package org.cryptimeleon.incentivesystem.dsprotectionservice;

import lombok.Getter;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Encapsulates all cryptographic assets/objects that the double-spending protection service needs.
 * This includes the public parameters of the underlying incentive system.
 */
@Repository
public class CryptoRepository {
    public static final int MAX_TRIES = 5; // number of tries that the repo should reconnect to the info service for querying the assets

    private Logger logger = LoggerFactory.getLogger(CryptoRepository.class);

    @Getter
    private IncentivePublicParameters pp;

    // TODO: implement InfoService connection as in DeductService
}
