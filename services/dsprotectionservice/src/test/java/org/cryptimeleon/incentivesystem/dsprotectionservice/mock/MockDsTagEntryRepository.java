package org.cryptimeleon.incentivesystem.dsprotectionservice.mock;

import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.DsTagEntry;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.DsTagEntryRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class MockDsTagEntryRepository implements DsTagEntryRepository {
    private HashMap<Long, DsTagEntry> doubleSpendingTagEntries; // stores entries for all transactions in the repo
    private long nextId; // ID to give to the next entry you add to your database

    public MockDsTagEntryRepository() {
        doubleSpendingTagEntries = new HashMap<>();
        nextId = 1;
    }

    /**
     * Stores new entry in DB, returns stored entry.
     */
    public DsTagEntry save(DsTagEntry dsTagEntry) {
        doubleSpendingTagEntries.put(nextId, dsTagEntry);
        // Note: IDs are wrongly autogenerated here
        // (more precisely: always 0)
        // since we do not boot up H2 framework (which would generate incrementing IDs automatically) when using this mock repository
        // => we need to manually set the ID of the new entry to the respective hash map key
        dsTagEntry.setId(nextId);
        nextId++;
        return dsTagEntry;
    }

    /**
     * Stores all entries from a passed list in the DB, returns the said list.
     */
    public <S extends DsTagEntry> Iterable<S> saveAll(Iterable<S> dsTagEntries) {
        for (DsTagEntry dste : dsTagEntries) {
            doubleSpendingTagEntries.put(nextId, dste);
            // Note: IDs are wrongly autogenerated here
            // (more precisely: always 0)
            // since we do not boot up H2 framework (which would generate incrementing IDs automatically) when using this mock repository
            // => we need to manually set the ID of the new entry to the respective hash map key
            dste.setId(nextId);
            nextId++;
        }
        return dsTagEntries;
    }

    /**
     * Returns the entry with the specified id.
     */
    public Optional<DsTagEntry> findById(Long id) {
        return Optional.ofNullable(doubleSpendingTagEntries.get(id));
    }

    /**
     * Returns true if and only if an entry with the passed id exists in the DB.
     */
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    /**
     * Retrieves all entries currently stored in the database.
     *
     * @return ArrayList of transaction entry objects
     */
    public ArrayList<DsTagEntry> findAll() {
        ArrayList<DsTagEntry> results = new ArrayList<>();

        for (Long l : doubleSpendingTagEntries.keySet()) {
            results.add(doubleSpendingTagEntries.get(l));
        }

        return results;
    }

    /**
     * Retrieves the entries with the passed ids.
     *
     * @return ArrayList of transaction entry objects
     */
    public ArrayList<DsTagEntry> findAllById(Iterable<Long> ids) {
        ArrayList<DsTagEntry> results = new ArrayList<>();

        for (long id : ids) {
            Optional<DsTagEntry> dste = findById(id);
            dste.ifPresent(results::add);
        }

        return results;
    }

    /**
     * Returns number of entries that the database currently contains.
     */
    public long count() {
        return doubleSpendingTagEntries.size();
    }

    /**
     * Deletes the entry with the passed id from the database.
     */
    public void deleteById(Long id) {
        doubleSpendingTagEntries.remove(id);
    }

    /**
     * Deletes the passed entry from the database if contained.
     */
    public void delete(DsTagEntry dsTagEntry) {
        for (long l : doubleSpendingTagEntries.keySet()) {
            if (doubleSpendingTagEntries.get(l).equals(dsTagEntry)) {
                doubleSpendingTagEntries.remove(l);
                return;
            }
        }
    }

    /**
     * Deletes all entries from the passed iterable from the database (if an entry is not contained in the database, it is skipped).
     */
    public void deleteAll(Iterable<? extends DsTagEntry> dsTagEntries) {
        for (DsTagEntry dste : dsTagEntries) {
            this.delete(dste);
        }
    }

    /**
     * Clears the database.
     */
    public void deleteAll() {
        this.doubleSpendingTagEntries = new HashMap<>();
    }
}
