package org.cryptimeleon.incentivesystem.dsprotectionservice.mock;


import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.UserInfoEntry;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.UserInfoRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class MockUserInfoEntryRepository implements UserInfoRepository {
    private HashMap<Long, UserInfoEntry> userInfoEntries; // stores entries for all transactions in the repo
    private long nextId; // ID to give to the next entry you add to your database

    public MockUserInfoEntryRepository() {
        userInfoEntries = new HashMap<Long, UserInfoEntry>();
        nextId = 0;
    }

    /**
     * Stores new entry in DB, returns stored entry.
     */
    public UserInfoEntry save(UserInfoEntry uinfoEntry) {
        userInfoEntries.put(nextId, uinfoEntry);
        nextId++;
        return uinfoEntry;
    }

    /**
     * Stores all entries from a passed list in the DB, returns the said list.
     */
    public <S extends UserInfoEntry> Iterable<S> saveAll(Iterable<S> uinfoEntries) {
        for(UserInfoEntry uie:uinfoEntries) {
            userInfoEntries.put(nextId, uie);
            nextId++;
        }
        return uinfoEntries;
    }

    /**
     * Returns the entry with the specified id.
     */
    public Optional<UserInfoEntry> findById(Long id) {
        return Optional.ofNullable(userInfoEntries.get(id));
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
    public ArrayList<UserInfoEntry> findAll() {
        ArrayList<UserInfoEntry> results = new ArrayList<UserInfoEntry>();

        for(Long l: userInfoEntries.keySet()) {
            results.add(userInfoEntries.get(l));
        }

        return results;
    }

    /**
     * Retrieves the entries with the passed ids.
     * @return ArrayList of transaction entry objects
     */
    public ArrayList<UserInfoEntry> findAllById(Iterable<Long> ids) {
        ArrayList<UserInfoEntry> results = new ArrayList<UserInfoEntry>();

        for(long id:ids) {
            UserInfoEntry uie = findById(id).get();
            if(uie!=null) {
                results.add(uie);
            }
        }

        return results;
    }

    /**
     * Returns number of entries that the database currently contains.
     */
    public long count() {
        return userInfoEntries.size();
    }

    /**
     * Deletes the entry with the passed id from the database.
     */
    public void deleteById(Long id) {
        userInfoEntries.remove(id);
    }

    /**
     * Deletes the passed entry from the database if contained.
     */
    public void delete(UserInfoEntry uie) {
        for(long l: userInfoEntries.keySet()) {
            if(userInfoEntries.get(l).equals(uie)) {
                userInfoEntries.remove(l);
                return;
            }
        }
    }

    /**
     * Deletes all entries from the passed iterable from the database (if an entry is not contained in the database, it is skipped).
     */
    public void deleteAll(Iterable<? extends UserInfoEntry> uiEntries) {
        for(UserInfoEntry uie:uiEntries) {
            this.delete(uie);
        }
    }

    /**
     * Clears the database.
     */
    public void deleteAll() {
        this.userInfoEntries = new HashMap<Long, UserInfoEntry>();
    }
}
