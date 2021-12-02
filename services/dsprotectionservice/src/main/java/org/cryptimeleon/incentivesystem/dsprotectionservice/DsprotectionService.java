package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class DsprotectionService {
    CryptoRepository cryptoRepository;

    LocalDatabaseHandler localDbHandler;

    /**
     * Default constructor to be executed when an object of this class is used as a Spring bean.
     */
    @Autowired
    private DsprotectionService(CryptoRepository cr) {
        this.cryptoRepository = cr;
        this.localDbHandler = new LocalDatabaseHandler(cryptoRepository.getPp());
    }

    /**
     * Executes dbSync for the passed (serialized) transaction data, i.e. records it in the database.
     * @param serializedTidRepr serialized transaction identifier representation
     * @param serializedDsidRepr serialized double-spending ID representation
     * @param serializedDsTagRepr serialized double-spending tag representation
     * @param spendAmount amount of points spent in the transaction
     */
    public void dbSync(String serializedTidRepr, String serializedDsidRepr, String serializedDsTagRepr, String spendAmount) {
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
        BigInteger k = new BigInteger(spendAmount);

        // actual call to dbSync
        IncentiveSystem theIncSys = cryptoRepository.getIncSys();
        theIncSys.dbSync(
                tid,
                dsid,
                dsTag,
                k,
                this.localDbHandler
        );
    }

    /**
     * Clears all tables of the double-spending database.
     * Needed for test runs where different test scenarios are created without restarting the double-spending protection service after each test.
     */
    public void clearDatabase() {
        localDbHandler.clearDatabase();
    }
}
