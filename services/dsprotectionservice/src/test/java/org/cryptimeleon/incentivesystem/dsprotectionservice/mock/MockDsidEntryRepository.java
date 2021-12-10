package org.cryptimeleon.incentivesystem.dsprotectionservice.mock;

import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.DsIdEntry;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.DsidRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class MockDsidEntryRepository implements DsidRepository {
    private HashMap<Long, DsIdEntry> doubleSpendingIdEntries; // stores entries for all transactions in the repo
    private long nextId; // ID to give to the next entry you add to your database

    public MockDsidEntryRepository() {
        doubleSpendingIdEntries = new HashMap<Long, DsIdEntry>();
        nextId = 0;
    }

    /**
     * Stores new entry in DB, returns stored entry.
     */
    public DsIdEntry save(DsIdEntry dsidEntry) {
        doubleSpendingIdEntries.put(nextId, dsidEntry);
        nextId++;
        return dsidEntry;
    }

    /**
     * Stores all entries from a passed list in the DB, returns the said list.
     */
    public <S extends DsIdEntry> Iterable<S> saveAll(Iterable<S> dsidEntries) {
        for(DsIdEntry tae:dsidEntries) {
            doubleSpendingIdEntries.put(nextId, tae);
            nextId++;
        }
        return dsidEntries;
    }

    /**
     * Returns the entry with the specified id.
     */
    public Optional<DsIdEntry> findById(Long id) {
        return Optional.ofNullable(doubleSpendingIdEntries.get(id));
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
    public ArrayList<DsIdEntry> findAll() {
        ArrayList<DsIdEntry> results = new ArrayList<DsIdEntry>();

        for(Long l:doubleSpendingIdEntries.keySet()) {
            results.add(doubleSpendingIdEntries.get(l));
        }

        return results;
    }

    /**
     * Retrieves the entries with the passed ids.
     * @return ArrayList of transaction entry objects
     */
    public ArrayList<DsIdEntry> findAllById(Iterable<Long> ids) {
        ArrayList<DsIdEntry> results = new ArrayList<DsIdEntry>();

        for(long id:ids) {
            DsIdEntry dside = findById(id).get();
            if(dside!=null) {
                results.add(dside);
            }
        }

        return results;
    }

    /**
     * Returns number of entries that the database currently contains.
     */
    public long count() {
        return doubleSpendingIdEntries.size();
    }

    /**
     * Deletes the entry with the passed id from the database.
     */
    public void deleteById(Long id) {
        doubleSpendingIdEntries.remove(id);
    }

    /**
     * Deletes the passed entry from the database if contained.
     */
    public void delete(DsIdEntry dsidEntry) {
        for(long l: doubleSpendingIdEntries.keySet()) {
            if(doubleSpendingIdEntries.get(l).equals(dsidEntry)) {
                doubleSpendingIdEntries.remove(l);
                return;
            }
        }
    }

    /**
     * Deletes all entries from the passed iterable from the database (if an entry is not contained in the database, it is skipped).
     */
    public void deleteAll(Iterable<? extends DsIdEntry> dsidEntries) {
        for(DsIdEntry dside:dsidEntries) {
            this.delete(dside);
        }
    }

    /**
     * Clears the database.
     */
    public void deleteAll() {
        this.doubleSpendingIdEntries = new HashMap<Long, DsIdEntry>();
    }
}
