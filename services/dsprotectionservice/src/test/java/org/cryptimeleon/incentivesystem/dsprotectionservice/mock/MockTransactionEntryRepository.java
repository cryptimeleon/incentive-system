package org.cryptimeleon.incentivesystem.dsprotectionservice.mock;

import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.TransactionEntry;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.TransactionEntryRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class MockTransactionEntryRepository implements TransactionEntryRepository {
    private HashMap<Long, TransactionEntry> transactionEntries; // stores entries for all transactions in the repo
    private long nextId; // ID to give to the next entry you add to your database

    public MockTransactionEntryRepository() {
        transactionEntries = new HashMap<Long, TransactionEntry>();
        nextId = 0;
    }

    /**
     * Stores new entry in DB, returns stored entry.
     */
    public TransactionEntry save(TransactionEntry taEntry) {
        transactionEntries.put(nextId, taEntry);
        nextId++;
        return taEntry;
    }

    /**
     * Stores all transaction entries from a passed list in the DB, returns the said list.
     */
    public <S extends TransactionEntry> Iterable<S> saveAll(Iterable<S> taEntries) {
        for(TransactionEntry tae:taEntries) {
            transactionEntries.put(nextId, tae);
            nextId++;
        }
        return taEntries;
    }

    /**
     * Returns the entry with the specified id.
     */
    public Optional<TransactionEntry> findById(Long id) {
        return Optional.ofNullable(transactionEntries.get(id));
    }

    /**
     * Returns true if and only if an entry with the passed id exists in the DB.
     */
    public boolean existsById(Long id) {
        return findById(id) != null;
    }

    /**
     * Retrieves all entries currently stored in the database.
     * @return ArrayList of transaction entry objects
     */
    public ArrayList<TransactionEntry> findAll() {
        ArrayList<TransactionEntry> results = new ArrayList<TransactionEntry>();

        for(Long l:transactionEntries.keySet()) {
            results.add(transactionEntries.get(l));
        }

        return results;
    }

    /**
     * Retrieves the entries with the passed ids.
     * @return ArrayList of transaction entry objects
     */
    public ArrayList<TransactionEntry> findAllById(Iterable<Long> ids) {
        ArrayList<TransactionEntry> results = new ArrayList<TransactionEntry>();

        for(long id:ids) {
            TransactionEntry tae = findById(id).get();
            if(tae!=null) {
                results.add(tae);
            }
        }

        return results;
    }

    /**
     * Returns number of entries that the database currently contains.
     */
    public long count() {
        return transactionEntries.size();
    }

    /**
     * Deletes the entry with the passed id from the database.
     */
    public void deleteById(Long id) {
        transactionEntries.remove(id);
    }

    /**
     * Deletes the passed transaction entry from the database if contained.
     */
    public void delete(TransactionEntry taEntry) {
        for(long l: transactionEntries.keySet()) {
            if(transactionEntries.get(l).equals(taEntry)) {
                transactionEntries.remove(l);
                return;
            }
        }
    }

    /**
     * Deletes all entries from the passed iterable from the database (if an entry is not contained in the database, it is skipped).
     * @param taEntries
     */
    public void deleteAll(Iterable<? extends TransactionEntry> taEntries) {
        for(TransactionEntry tae:taEntries) {
            this.delete(tae);
        }
    }

    /**
     * Clears the database.
     */
    public void deleteAll() {
        this.transactionEntries = new HashMap<Long, TransactionEntry>();
    }
}
