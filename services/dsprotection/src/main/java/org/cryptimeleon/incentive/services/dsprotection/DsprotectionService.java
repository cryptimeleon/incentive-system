package org.cryptimeleon.incentive.services.dsprotection;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.incentive.services.dsprotection.storage.TransactionEntryRepository;
import org.cryptimeleon.incentive.services.dsprotection.storage.DsTagEntryRepository;
import org.cryptimeleon.incentive.services.dsprotection.storage.DsidRepository;
import org.cryptimeleon.incentive.services.dsprotection.storage.UserInfoRepository;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j // auto-generates logger field
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
     *
     *
     *
     * @param serializedTidRepr   serialized transaction identifier representation
     * @param serializedDsidRepr  serialized double-spending ID representation
     * @param serializedDsTagRepr serialized double-spending tag representation
     * @param promotionId         identifier for the promotion that user took part in with the transaction
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

    // TODO: at least this endpoint needs to be protected by a shared secret, if time, protect all the others too
    /**
     * Clears all tables of the double-spending database.
     * Needed for test runs where different test scenarios are created without restarting the double-spending protection service after each test.
     */
    public void clearDatabase() {
        localDbHandler.clearDatabase();
    }

    /**
     * Returns the transaction with the specified transaction identifier from the database if contained.
     * @param serializedTaIdentifierRepr serialized representation of a transaction identifier, consisting of a numerical ID and the challenge generator gamma
     * @return Transaction object (crypto)
     */
    public String getTransaction(String serializedTaIdentifierRepr) {
        // deserialze and restore ID
        JSONConverter jsonConverter = new JSONConverter();
        TransactionIdentifier taIdentifier = new TransactionIdentifier(
                jsonConverter.deserialize(serializedTaIdentifierRepr),
                cryptoRepository.getPp()
        );

        // query result from database
        Transaction ta = localDbHandler.getTransactionNode(taIdentifier);

        // represent and serialize transaction
        return Util.computeSerializedRepresentation(ta);
    }

    /**
     * Returns true if and only if the passed token double-spending identifier (dsid) is already contained in the database.
     * @param serializedDsidRepr serialized representation of a token dsid (G1 group element)
     */
    public boolean containsDsid(String serializedDsidRepr) {
        // deserialize data
        JSONConverter jsonConverter = new JSONConverter();
        Representation dsidRepr = jsonConverter.deserialize(serializedDsidRepr);
        Group g1Group = cryptoRepository.getPp().getBg().getG1();
        GroupElement dsid = g1Group.restoreElement(dsidRepr);

        // make call to db handler + return result
        return localDbHandler.containsTokenNode(dsid);
    }
}
