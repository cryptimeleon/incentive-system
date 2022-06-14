package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.crypto.dsprotectionlogic.DatabaseHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.*;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Implements the double-spending protection database access functionality when having direct (i.e. non-remote) access to said database.
 */
public class LocalDatabaseHandler implements DatabaseHandler {
    DsidRepository dsidRepository;
    TransactionEntryRepository transactionRepository;
    DsTagEntryRepository doubleSpendingTagRepository;
    UserInfoRepository userInfoRepository;

    private Logger logger = LoggerFactory.getLogger(LocalDatabaseHandler.class);
    private IncentivePublicParameters pp;

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
        if (taEntry == null) {
            throw new RuntimeException("No transaction corresponding to the queried identifier was found in database.");
        } else if (dsIdEntry == null) {
            throw new RuntimeException("Queried double-spending ID not found in database.");
        } else {
            transactionRepository.delete(taEntry);
            taEntry.setProducedDsidEntryId(dsIdEntry.getId());
            transactionRepository.save(taEntry);
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

        // make edge (i.e. update field of ta entry that holds ID of consumed token's dsid) if both nodes exist
        if (taEntry == null) {
            throw new RuntimeException("No transaction corresponding to the queried identifier was found in database.");
        } else if (dsIdEntry == null) {
            throw new RuntimeException("Queried double-spending ID not found in database.");
        } else {
            transactionRepository.delete(taEntry);
            taEntry.setConsumedDsidEntryId(dsIdEntry.getId());
            transactionRepository.save(taEntry);
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

        return taEntry != null &&
                dsIdEntry != null &&
                taEntry.getProducedDsidEntryId() == dsIdEntry.getId();
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

        return taEntry != null &&
                dsIdEntry != null &&
                dsIdEntry.getId() == taEntry.getConsumedDsidEntryId();
    }

    /**
     * Adds the passed user info as an entry to the database and links it to the entry for the passed dsid.
     */
    @Override
    public void addAndLinkUserInfo(UserInfo userInfo, GroupElement dsid) {
        // creating user info entry object
        UserInfoEntry uie = new UserInfoEntry(userInfo);

        // add user info entry to database
        this.userInfoRepository.save(uie);

        // Update dsid entry if existent.
        // This means deleting and adding the dsid entry again:
        // this changes its id => need to update consuming and producing transactions!
        long uInfoEntryId = uie.getId();
        DsIdEntry dside = findDsidEntry(dsid);
        long oldDsidEntryId = 0;
        long newDsidEntryId = 0;
        if (dside != null) {
            oldDsidEntryId = dside.getId();
            dsidRepository.delete(dside);
            dside.setAssociatedUserInfoId(uInfoEntryId);
            dsidRepository.save(dside);
            newDsidEntryId = dside.getId();
        }

        // update consuming transactions
        ArrayList<TransactionEntry> consumingTasEntries = this.getConsumingTransactionEntries(oldDsidEntryId);
        for (TransactionEntry taEntry : consumingTasEntries) {
            taEntry.setConsumedDsidEntryId(newDsidEntryId);
        }

        // update producing transactions
        ArrayList<TransactionEntry> producingTasEntries = this.getProducingTransactionEntries(oldDsidEntryId);
        for (TransactionEntry taEntry : producingTasEntries) {
            taEntry.setProducedDsidEntryId(newDsidEntryId);
        }
    }

    /**
     * Retrieves the (anonymized) user info associated to the token identified by the passed dsid.
     * If either passed dsid is not contained in database or has no user info associated to it,
     * null is returned.
     */
    @Override
    public UserInfo getUserInfo(GroupElement dsid) {
        // query user info from database
        DsIdEntry dside = findDsidEntry(dsid);

        // storage variables to shorten code
        UserInfo uInfo;
        UserInfoEntry uie;
        long uieId;

        // Attempt to retrieve user info entry associated to passed dsid.
        if (dside != null) {
            uieId = dside.getAssociatedUserInfoId();
            if (userInfoRepository.findById(uieId).isPresent()) {
                uie = userInfoRepository.findById(uieId).get();
            } else {
                return null;
            }
        } else {
            return null;
        }

        // if successfully retrieved: convert user info entry to user info
        uInfo = convertUserInfoEntry(uie);

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
        DsIdEntry consumedDsidEntry = dsidRepository.findById(taEntry.getConsumedDsidEntryId()).orElseThrow();

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
        if (tae != null) {
            transactionRepository.delete(tae);
            tae.invalidate();
            transactionRepository.save(tae);
        }
    }


    /*
     * helper methods
     */


    /**
     * Converts a transaction database entry to a normal (crypto) transaction.
     * The original object is not changed.
     *
     * @param taEntry original transaction entry
     * @return transaction object
     */
    private Transaction convertTransactionEntry(TransactionEntry taEntry) {
        // retrieve entry of the double-spending tag corresponding to the transaction entry in question
        DsTagEntry taDsTagEntry = doubleSpendingTagRepository.findById(taEntry.getDsTagEntryId()).orElseThrow();

        // deserialize representation of transaction ID
        JSONConverter jsonConverter = new JSONConverter();
        Representation transactionIDRepr = jsonConverter.deserialize(taEntry.getSerializedTransactionIDRepr());

        // assemble crypto transaction object
        return new Transaction(
                taEntry.isValid(),
                this.pp.getBg().getZn().restoreElement(transactionIDRepr),
                new BigInteger(taEntry.getK()),
                new DoubleSpendingTag(
                        this.pp,
                        taDsTagEntry.getSerializedC0Repr(),
                        taDsTagEntry.getSerializedC1Repr(),
                        taDsTagEntry.getSerializedGammaRepr(),
                        taDsTagEntry.getSerializedEskStarProvRepr(),
                        taDsTagEntry.getSerializedCTrace0Repr(),
                        taDsTagEntry.getSerializedCTrace1Repr()
                )
        );
    }

    /**
     * Converts a user info database entry object to a normal (crypto) user info object.
     * The original object is not changed.
     *
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
     * Returns an array list containing all transactions that are contained in the database.
     * Needed for double-spending protection service front-end to retrieve data from backend.
     * @return array list of transactions
     */
    public ArrayList<Transaction> getAllTransactions() {
        // query all transaction entries from database
        ArrayList<TransactionEntry> taEntryList = (ArrayList<TransactionEntry>) transactionRepository.findAll();

        // convert entries to crypto objects
        ArrayList<Transaction> resultList = new ArrayList<>();
        for(TransactionEntry taEntry:taEntryList) {
            Transaction ta = convertTransactionEntry(taEntry);
            resultList.add(ta);
        }

        // return result
        return resultList;

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

            if (taeTid.equals(tid)) {
                // retrieve corresponding double-spending tag entry
                DsTagEntry dste = doubleSpendingTagRepository.findById(tae.getDsTagEntryId()).orElseThrow();

                // deserialize gamma of currently considered transaction entry
                Zn.ZnElement dsteGamma = usedZn.restoreElement(jsonConverter.deserialize(dste.getSerializedGammaRepr()));

                if (dsteGamma.equals(gamma)) {
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
        for (DsIdEntry dside : dsidEntryList) {
            // deserialize dsid of currently considered dsid entry
            GroupElement dsideDsid = groupG1.restoreElement(jsonConverter.deserialize(dside.getSerializedDsidRepr()));

            if (dsideDsid.equals(dsid)) {
                return dside;
            }
        }

        // drop-down if nothing found
        return null;
    }

    /**
     * Retrieves and returns all transactions that have consumed a Dsid whose corresponding database entry has the passed ID.
     *
     * @param dsidEntryId database entry ID
     * @return list of transactions
     */
    public ArrayList<TransactionEntry> getConsumingTransactionEntries(long dsidEntryId) {
        ArrayList<TransactionEntry> resultList = new ArrayList<>();

        // query all transaction entries from database
        ArrayList<TransactionEntry> transactionEntryList = (ArrayList<TransactionEntry>) transactionRepository.findAll();

        // filter by consumed dsid entry ID
        for (TransactionEntry tae : transactionEntryList) {
            if (tae.getConsumedDsidEntryId() == dsidEntryId) {
                resultList.add(tae);
            }
        }

        return resultList;
    }

    /**
     * Retrieves and returns all transactions that have produced a Dsid whose corresponding database entry has the passed ID.
     *
     * @param dsidEntryId database entry ID
     * @return list of transactions
     */
    public ArrayList<TransactionEntry> getProducingTransactionEntries(long dsidEntryId) {
        ArrayList<TransactionEntry> resultList = new ArrayList<>();

        // query all transaction entries from database
        ArrayList<TransactionEntry> transactionEntryList = (ArrayList<TransactionEntry>) transactionRepository.findAll();

        // filter by produced dsid entry ID
        for (TransactionEntry tae : transactionEntryList) {
            if (tae.getProducedDsidEntryId() == dsidEntryId) {
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

    /**
     * Helper methods providing info about the database state.
     * Note that they are designed to not expose any information about the underlying database administration objects (like for example CRUDRepositories).
     */
    public long getTransactionCount() {
        return transactionRepository.count();
    }

    public long getTokenCount() {
        return dsidRepository.count();
    }

    public long getDsTagCount() {
        return doubleSpendingTagRepository.count();
    }

    public long getUserInfoCount() {
        return userInfoRepository.count();
    }
}
