package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.DsTagEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.DsidRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.TransactionEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.UserInfoRepository;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DsprotectionService {
    CryptoRepository cryptoRepository;

    LocalDatabaseHandler localDbHandler;

    /**
     * Default constructor to be executed when an object of this class is used as a Spring bean.
     * Autowired: actual parameters are collected from available Spring beans.
     */
    @Autowired
    private DsprotectionService(
            CryptoRepository cr,
            TransactionEntryRepository taEntryRepo,
            DsidRepository dsidRepo,
            DsTagEntryRepository dsTagEntryRepo,
            UserInfoRepository uInfoRepo
    ) {
        this.cryptoRepository = cr;
        this.localDbHandler = new LocalDatabaseHandler(cryptoRepository.getPp(), dsidRepo, taEntryRepo, dsTagEntryRepo, uInfoRepo);
    }

    /**
     * Executes dbSync for the passed (serialized) transaction data, i.e. records it in the database.
     *
     * @param serializedTidRepr   serialized transaction identifier representation
     * @param serializedDsidRepr  serialized double-spending ID representation
     * @param serializedDsTagRepr serialized double-spending tag representation
     * @param promotionId
     * @param userChoice          representing the type of reward that the user chose
     */
    public void dbSync(String serializedTidRepr, String serializedDsidRepr, String serializedDsTagRepr, BigInteger promotionId, String userChoice) {
        // deserialize and restore data
        JSONConverter jsonConverter = new JSONConverter();
        IncentivePublicParameters pp = cryptoRepository.getPp();
        Zn.ZnElement tid = pp.getBg().getZn().restoreElement(
                jsonConverter.deserialize(serializedTidRepr)
        );
        GroupElement dsid = pp.getBg().getG1().restoreElement(
                jsonConverter.deserialize(serializedDsidRepr)
        );
        DoubleSpendingTag dsTag = new DoubleSpendingTag(
                jsonConverter.deserialize(serializedDsTagRepr),
                pp
        );

        // actual call to dbSync
        IncentiveSystem theIncSys = cryptoRepository.getIncSys();
        theIncSys.dbSync(
                tid,
                dsid,
                dsTag,
                userChoice,
                promotionId,
                this.localDbHandler
        );
    }

    public List<TransactionDto> getAllTransactions() {
        // return list, map all crypto transaction objects to data transfer objects (DTOs)
        return this.localDbHandler.getAllTransactions().stream().map(TransactionDto::new).collect(Collectors.toList());
    }

    // TODO: this endpoint needs to be protected by a shared secret
    /**
     * Clears all tables of the double-spending database.
     * Needed for test runs where different test scenarios are created without restarting the double-spending protection service after each test.
     */
    public void clearDatabase() {
        localDbHandler.clearDatabase();
    }
}
