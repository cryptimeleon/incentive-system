package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.client.WebClientHelper;
import org.cryptimeleon.incentive.crypto.dsprotectionlogic.DatabaseHandler;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.*;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;

/**
 * Implements the double-spending protection database access functionality when having direct (i.e. non-remote) access to said database.
 */
public class LocalDatabaseHandler implements DatabaseHandler {
    private Logger logger = LoggerFactory.getLogger(LocalDatabaseHandler.class);

    private WebClient dsProtectionClient;

    private IncentivePublicParameters pp;

    DsidRepository dsidRepository;

    TransactionEntryRepository transactionRepository;

    DsTagEntryRepository doubleSpendingTagRepository;

    UserInfoRepository userInfoRepository;

    public LocalDatabaseHandler(IncentivePublicParameters pp) {
        this.pp = pp;
    }

    /**
     * Additional constructor for testing, allows to instantiate repository fields with hash map based mock repositories.
     */
    public LocalDatabaseHandler(
            IncentivePublicParameters pp,
            DsidRepository dsidRepo,
            TransactionEntryRepository taRepo,
            DsTagEntryRepository dsTagRepo,
            UserInfoRepository uinfoRepo
    ) {
        this.pp = pp;
        this.dsidRepository = dsidRepo;
        this.transactionRepository = taRepo;
        this.doubleSpendingTagRepository = dsTagRepo;
        this.userInfoRepository = uinfoRepo;
    }

    /**
     * Adds an entry for the passed spend transaction to the database.
     */
    @Override
    public void addTransactionNode(Transaction ta) {
        // create transaction and double-spending tag entry objects
        TransactionEntry taEntry = new TransactionEntry(ta);
        DsTagEntry dsTagEntry = new DsTagEntry(ta.getDsTag());

        // add double spending tag entry to database
        doubleSpendingTagRepository.save(dsTagEntry);

        // link newly added dstag to transaction
        long dsTagEntryId = dsTagEntry.getId();
        taEntry.setDsTagEntryId(dsTagEntryId);

        // add transaction entry object (with linked dstag) to database
        transactionRepository.save(taEntry);
    }

    /**
     * Finds and returns the transaction that is identified by the passed transaction identifier.
     */
    @Override
    public Transaction getTransactionNode(TransactionIdentifier taId) {
        TransactionEntry taEntry = findTransactionEntryWithTaIdentifier(taId);
        return this.convertTransactionEntry(taEntry);
    }

    /**
     * Adds a new token node for a token with the passed dsid to the database.
     */
    @Override
    public void addTokenNode(GroupElement dsid) {
        // create dsid entry object
        DsIdEntry dsIdEntry = new DsIdEntry(dsid);

        // add dsid entry object to database
        dsidRepository.save(dsIdEntry);
    }

    /**
     * Makes an edge from the passed transaction to the passed double-spending ID, if nodes for both exist.
     * Semantics of this edge: the passed transaction produced the token with the passed double-spending ID
     */
    @Override
    public void addTransactionTokenEdge(TransactionIdentifier taId, GroupElement dsid) {
        // retrieve entries for respective transaction and dsID
        TransactionEntry taEntry = findTransactionEntryWithTaIdentifier(taId);
        DsIdEntry dsIdEntry = findDsidEntry(dsid);

        // make edge (i.e. update field of ta entry that holds ID of produced token's dsid) if both nodes exist
        if(taEntry != null && dsIdEntry != null) {
            transactionRepository.delete(taEntry);
            taEntry.setProducedDsidEntryId(dsIdEntry.getId());
            transactionRepository.save(taEntry);
        }
        // error report if either transaction or dsid was not found in the database
        else {
            if (taEntry == null) {
                throw new RuntimeException("No transaction corresponding to the queried identifier was found in database.");
            }
            if (dsIdEntry == null) {
                throw new RuntimeException("Queried double-spending ID not found in database.");
            }
            throw new RuntimeException("An unknown error occurred while adding transaction-token edge. Aborting, no write operations performed on database.");
        }
    }

    /**
     * Makes an edge from the passed double-spending ID to the passed transaction (if both nodes exist).
     * Semantics of this edge: the token with the passed double-spending ID was consumed in the passed transaction
     */
    @Override
    public void addTokenTransactionEdge(GroupElement dsid, TransactionIdentifier taId) {
        // retrieve entries for respective transaction and dsID
        TransactionEntry taEntry = findTransactionEntryWithTaIdentifier(taId);
        DsIdEntry dsIdEntry = findDsidEntry(dsid);

        // make edge (i.e. update field of transaction entry that holds ID of consumed dsid's entry) if both nodes exist
        if(taEntry != null && dsIdEntry != null) {
            transactionRepository.delete(taEntry);
            taEntry.setConsumedDsidEntryId(dsIdEntry.getId());
            transactionRepository.save(taEntry);
        }
        // error report if either transaction or dsid was not found in the database
        else {
            if (taEntry == null) {
                throw new RuntimeException("No transaction corresponding to the queried identifier was found in database.");
            }
            if (dsIdEntry == null) {
                throw new RuntimeException("Queried double-spending ID not found in database.");
            }
            throw new RuntimeException("An unknown error occurred while adding token-transaction edge. Aborting, no write operations performed on database.");
        }
    }

    /**
     * Checks the transaction table for containment of a transaction with the passed identifier.
     */
    @Override
    public boolean containsTransactionNode(TransactionIdentifier taIdentifier) {
        return findTransactionEntryWithTaIdentifier(taIdentifier) != null;
    }

    /**
     * Checks the double-spending ID table for containment of the passed dsid.
     */
    @Override
    public boolean containsTokenNode(GroupElement dsid) {
        return findDsidEntry(dsid) != null;
    }

    /**
     * Checks the database for a connection from passed transaction to token with passed double-spending ID.
     * So this method checks whether said transaction produced said token.
     * Result is returned as HTTP response object.
     */
    @Override
    public boolean containsTransactionTokenEdge(TransactionIdentifier taId, GroupElement dsid) {
        // retrieve entries for respective transaction and dsID
        TransactionEntry taEntry = findTransactionEntryWithTaIdentifier(taId);
        DsIdEntry dsIdEntry = findDsidEntry(dsid);

        boolean edgeExists = taEntry != null &&
                dsIdEntry != null &&
                taEntry.getProducedDsidEntryId() == dsIdEntry.getId();

        return edgeExists;
    }

    /**
     * Checks the database for a connection from token with passed double-spending ID to passed transaction.
     * So this method checks whether said token was consumed in said transaction.
     */
    @Override
    public boolean containsTokenTransactionEdge(GroupElement dsid, TransactionIdentifier taId) {
        // retrieve entries for respective transaction and dsID
        TransactionEntry taEntry = findTransactionEntryWithTaIdentifier(taId);
        DsIdEntry dsIdEntry = findDsidEntry(dsid);

        // check for existence of edge
        boolean edgeExists = taEntry != null &&
                dsIdEntry != null &&
                dsIdEntry.getId() == taEntry.getConsumedDsidEntryId();

        return edgeExists;
    }

    /**
     * Adds the passed user info as an entry to the database and links it to the entry for the passed dsid.
     */
    @Override
    public void addAndLinkUserInfo(UserInfo userInfo, GroupElement dsid) {
        // creating user info entry object
        UserInfoEntry uie = new UserInfoEntry(userInfo);

        // update dsid entry if existent
        long uInfoEntryId = uie.getId();
        DsIdEntry dside = findDsidEntry(dsid);
        if(dside != null) {
            dsidRepository.delete(dside);
            dside.setAssociatedUserInfoId(uInfoEntryId);
            dsidRepository.save(dside);
        }
    }

    /**
     * Retrieves the (anonymized) user info associated to the token identified by the passed dsid.
     */
    @Override
    public UserInfo getUserInfo(GroupElement dsid) {
        // query user info from database
        DsIdEntry dside = findDsidEntry(dsid);
        long uieId = dside.getAssociatedUserInfoId();
        UserInfoEntry uie = userInfoRepository.findById(uieId).get();

        // convert user info entry to user info
        UserInfo uInfo = convertUserInfoEntry(uie);

        return uInfo;
    }

    /**
     * Retrieves all transactions that consumed the token with the passed dsid.
     */
    @Override
    public ArrayList<Transaction> getConsumingTransactions(GroupElement dsid) {
        // query respective entries
        ArrayList<TransactionEntry> taeList = getConsumingTransactionEntries(
                findDsidEntry(dsid).getId()
        );

        ArrayList<Transaction> taList = new ArrayList<>();

        taeList.forEach(tae -> {
            Transaction ta = convertTransactionEntry(tae);
            taList.add(ta);
        });

        return taList;
    }

    /**
     * Retrieves the double-spending ID of the token that was consumed in the transaction with the passed identifier.
     */
    @Override
    public GroupElement getConsumedTokenDsid(TransactionIdentifier taId, IncentivePublicParameters pp) {
        // find transaction entry
        TransactionEntry taEntry = findTransactionEntryWithTaIdentifier(taId);

        // find dsid entry of consumed token's double-spending ID
        DsIdEntry consumedDsidEntry = dsidRepository.findById(taEntry.getConsumedDsidEntryId()).get();

        String serializedConsumedDsidRepr = consumedDsidEntry.getSerializedDsidRepr();

        // deserializing and restoring
        JSONConverter jsonConverter = new JSONConverter();
        return pp.getBg().getG1().restoreElement(
                jsonConverter.deserialize(serializedConsumedDsidRepr)
        );
    }

    /**
     * Marks transaction with passed identifier as invalid in the database (if contained).
     */
    @Override
    public void invalidateTransaction(TransactionIdentifier taIdentifier) {
        // if existent: update respective transaction entry (retrieve, then delete from DB; add modified entry)
        TransactionEntry tae = findTransactionEntryWithTaIdentifier(taIdentifier);
        if(tae != null) {
            transactionRepository.delete(tae);
            tae.invalidate();
            transactionRepository.save(tae);
        }
    }




    /**
     * helper methods
     */





    /**
     * Converts a transaction database entry to a normal (crypto) transaction.
     * The original object is not changed.
     * @param taEntry original transaction entry
     * @return transaction object
     */
    private Transaction convertTransactionEntry(TransactionEntry taEntry) {
        DsTagEntry taDsTagEntry = doubleSpendingTagRepository.findById(taEntry.getDsTagEntryId()).get();
        return new Transaction(
                this.pp,
                taEntry.isValid(),
                taEntry.getSerializedTransactionIDRepr(),
                taEntry.getK(),
                taDsTagEntry.getSerializedC0Repr(),
                taDsTagEntry.getSerializedC1Repr(),
                taDsTagEntry.getSerializedGammaRepr(),
                taDsTagEntry.getSerializedEskStarProvRepr(),
                taDsTagEntry.getSerializedCTrace0Repr(),
                taDsTagEntry.getSerializedCTrace1Repr()
        );
    }

    /**
     * Converts a user info database entry object to a normal (crypto) user info object.
     * The original object is not changed.
     * @param uiEntry original user info entry
     * @return user info object
     */
    private UserInfo convertUserInfoEntry(UserInfoEntry uiEntry) {
        // create JSON converter and shorthand for the ring Zn used in the system
        JSONConverter jsonConverter = new JSONConverter();
        Zn usedZn = this.pp.getBg().getZn();

        // restore fields from representations
        UserPublicKey upk = new UserPublicKey(
                jsonConverter.deserialize(uiEntry.getSerializedUpkRepr()),
                this.pp.getBg().getG1()
        );
        Zn.ZnElement dsBlame = usedZn.restoreElement(
                jsonConverter.deserialize(uiEntry.getSerializedDsBlameRepr())
        );
        Zn.ZnElement dsTrace = usedZn.restoreElement(
                jsonConverter.deserialize(uiEntry.getSerializedDsTraceRepr())
        );

        return new UserInfo(upk, dsBlame, dsTrace);
    }

    /**
     * Retrieves and returns the transaction entry for the transaction with the passed transaction ID and gamma if existent.
     */
    private TransactionEntry findTransactionEntryWithTaIdentifier(TransactionIdentifier taIdentifier) {
        Zn.ZnElement tid = taIdentifier.getTid();
        Zn.ZnElement gamma = taIdentifier.getGamma();

        // query all transaction entries from database
        ArrayList<TransactionEntry> taEntryList = (ArrayList<TransactionEntry>) transactionRepository.findAll();


        JSONConverter jsonConverter = new JSONConverter(); // need a JSON converter for deserializing tids and gammas of transaction entries (required for comparison)
        Zn usedZn = this.pp.getBg().getZn();// store used ZN to shorten code

        // look for one with fitting tid and gamma and return it
        for (TransactionEntry tae : taEntryList) {
            // deserialize tid of currently considered transaction entry
            Zn.ZnElement taeTid = usedZn.restoreElement(jsonConverter.deserialize(tae.getSerializedTransactionIDRepr()));

            if(taeTid.equals(tid)) {
                // retrieve corresponding double-spending tag entry
                DsTagEntry dste = doubleSpendingTagRepository.findById(tae.getDsTagEntryId()).get();

                // deserialize gamma of currently considered transaction entry
                Zn.ZnElement dsteGamma = usedZn.restoreElement(jsonConverter.deserialize(dste.getSerializedGammaRepr()));

                if(dsteGamma.equals(gamma)) {
                    return tae;
                }
            }
        }

        // drop-down if nothing found
        return null;
    }

    /**
     * Retrieves and returns the double-spending ID entry for the double-spending ID with the passed value (if existent).
     */
    private DsIdEntry findDsidEntry(GroupElement dsid) {
        // query all DSID entries from database
        ArrayList<DsIdEntry> dsidEntryList = (ArrayList<DsIdEntry>) dsidRepository.findAll();

        // create a JSON converter + store first group from used bilinear group (to shorten code)
        JSONConverter jsonConverter = new JSONConverter();
        Group groupG1 = this.pp.getBg().getG1();

        // look for one with fitting value and return it
        for(DsIdEntry dside : dsidEntryList) {
            // deserialize dsid of currently considered dsid entry
            GroupElement dsideDsid = groupG1.restoreElement(jsonConverter.deserialize(dside.getSerializedDsidRepr()));

            if(dsideDsid.equals(dsid)) {
                return dside;
            }
        }

        // drop-down if nothing found
        return null;
    }

    /**
     * Retrieves and returns all transactions that have consumed a Dsid whose corresponding database entry has the passed ID.
     * @param dsidEntryId database entry ID
     * @return list of transactions
     */
    public ArrayList<TransactionEntry> getConsumingTransactionEntries(long dsidEntryId) {
        ArrayList<TransactionEntry> resultList = new ArrayList<TransactionEntry>();

        // query all transaction entries from database
        ArrayList<TransactionEntry> transactionEntryList = (ArrayList<TransactionEntry>) transactionRepository.findAll();

        // filter by consumed dsid entry ID
        for(TransactionEntry tae : transactionEntryList) {
            if(tae.getConsumedDsidEntryId() == dsidEntryId) {
                resultList.add(tae);
            }
        }

        return resultList;
    }

    /**
     * Clears all tables of the double-spending database.
     * Needed for sequences of independent tests where double-spending protection service is not restarted in between.
     */
    public void clearDatabase() {
        this.transactionRepository.deleteAll();
        this.dsidRepository.deleteAll();
        this.doubleSpendingTagRepository.deleteAll();
        this.userInfoRepository.deleteAll();
    }
}
