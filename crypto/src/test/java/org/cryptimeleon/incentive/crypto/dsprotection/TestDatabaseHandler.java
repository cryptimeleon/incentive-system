package org.cryptimeleon.incentive.crypto.dsprotection;

import org.cryptimeleon.incentive.crypto.dsprotectionlogic.DatabaseHandler;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.math.structures.groups.GroupElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestDatabaseHandler implements DatabaseHandler {

    private final List<Transaction> transactionNodes = new ArrayList<>();
    private final List<GroupElement> tokenNodes = new ArrayList<>();
    private final Map<TransactionIdentifier, GroupElement> edgesFromTransactionToTokens = new HashMap<>(); // TODO 1:1 instead of 1:n?
    private final Map<GroupElement, ArrayList<TransactionIdentifier>> edgesFromTokenToTransactions = new HashMap<>();
    private final Map<GroupElement, UserInfo> userInfoMap = new HashMap<>();

    public TestDatabaseHandler() {
    }

    /**
     * Adds a new transaction node to the database.
     *
     * @param ta transaction to add
     */
    @Override
    public void addTransactionNode(Transaction ta) {
        transactionNodes.add(ta);
    }

    @Override
    public Transaction getTransactionNode(TransactionIdentifier taId) {
        return transactionNodes.stream()
                .filter(ta -> ta.getTransactionID().equals(taId.getTid()) && ta.getDsTag().getGamma().equals(taId.getGamma()))
                .findAny()
                .orElseThrow();
    }

    @Override
    public void addTokenNode(GroupElement dsid) {
        tokenNodes.add(dsid);
    }

    @Override
    public void addTransactionTokenEdge(TransactionIdentifier taId, GroupElement dsid) {
        edgesFromTransactionToTokens.put(taId, dsid);
    }

    @Override
    public void addTokenTransactionEdge(GroupElement dsid, TransactionIdentifier taId) {
        edgesFromTokenToTransactions.compute(dsid, (doubleSpendingID, transactionIdentifiers) -> {
            if (transactionIdentifiers == null) {
                return new ArrayList<>(List.of(taId));
            } else {
                transactionIdentifiers.add(taId);
                return transactionIdentifiers;
            }
        });
    }

    @Override
    public boolean containsTransactionNode(TransactionIdentifier taIdentifier) {
        return transactionNodes.stream()
                .anyMatch(ta -> ta.getTransactionID().equals(taIdentifier.getTid()) && ta.getDsTag().getGamma().equals(taIdentifier.getGamma()));
    }

    @Override
    public boolean containsTokenNode(GroupElement dsid) {
        return tokenNodes.contains(dsid);
    }

    @Override
    public boolean containsTransactionTokenEdge(TransactionIdentifier taId, GroupElement dsid) {
        return edgesFromTransactionToTokens.containsKey(taId) && edgesFromTransactionToTokens.get(taId).equals(dsid);
    }

    @Override
    public boolean containsTokenTransactionEdge(GroupElement dsid, TransactionIdentifier taId) {
        return edgesFromTokenToTransactions.containsKey(dsid) && edgesFromTokenToTransactions.get(dsid).contains(taId);
    }

    /**
     * Adds info about the user that spent a specific token to said token.
     *
     * @param userInfo user info
     * @param dsid     double-spending ID identifying the token
     */
    @Override
    public void addAndLinkUserInfo(UserInfo userInfo, GroupElement dsid) {
        userInfoMap.put(dsid, userInfo);
    }

    /**
     * Retrieves the user info associated to the passed double-spending ID.
     *
     * @param dsid
     */
    @Override
    public UserInfo getUserInfo(GroupElement dsid) {
        return userInfoMap.get(dsid);
    }

    /**
     * Retrieves all transactions that have consumed the passed double-spending ID.
     *
     * @param dsid
     */
    @Override
    public ArrayList<Transaction> getConsumingTransactions(GroupElement dsid) {
        return edgesFromTokenToTransactions.getOrDefault(dsid, new ArrayList<>())
                .stream()
                .map(this::getTransactionNode)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Retrieves the double-spending ID of the token that was consumed in the transaction with the passed identifier.
     *
     * @param taId
     * @return // TODO refactor not needed pp!
     */
    @Override
    public GroupElement getConsumedTokenDsid(TransactionIdentifier taId) {
        return edgesFromTokenToTransactions
                .entrySet().stream()
                .filter(entry -> entry.getValue().contains(taId))
                .findAny().orElseThrow()
                .getKey();
    }

    /**
     * Marks the transaction specified by the passed ID and challenge generator as invalid.
     *
     * @param taIdentifier
     */
    @Override
    public void invalidateTransaction(TransactionIdentifier taIdentifier) {
        // A little more complicated to keep transactions immutable
        var transactionToInvalidate = getTransactionNode(taIdentifier);
        transactionNodes.remove(transactionToInvalidate);
        var invalidatedTransaction = transactionToInvalidate.toBuilder().isValid(false).build();
        transactionNodes.add(invalidatedTransaction);
    }

    /**
     * Helper methods providing info about the database state.
     * Note that they are designed to not expose any information about the underlying database administration objects (like for example CRUDRepositories).
     */
    @Override
    public long getTransactionCount() {
        return transactionNodes.size();
    }

    @Override
    public long getTokenCount() {
        return tokenNodes.size();
    }

    @Override
    public long getDsTagCount() {
        // TODO I assume this is 1:1 with tokens? Why does this function exist in the interface?
        return transactionNodes.size();
    }

    @Override
    public long getUserInfoCount() {
        return userInfoMap.size();
    }

    public long getValidTransactionCount() {
        return transactionNodes.stream().filter(Transaction::getIsValid).count();
    }

    public long getInvalidTransactionCount() {
        return getTransactionCount() - getValidTransactionCount();
    }
}
