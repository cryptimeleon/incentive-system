package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.crypto.callback.IEarnTransactionDBHandler;
import org.cryptimeleon.incentive.crypto.callback.ISpendTransactionDBHandler;
import org.cryptimeleon.incentive.crypto.model.EarnTransactionData;
import org.cryptimeleon.incentive.crypto.model.SpendTransactionData;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Store all transaction for clearing and offline double-spending protection
 */
@Repository
public class TransactionRepository implements IEarnTransactionDBHandler, ISpendTransactionDBHandler {
    // Some data could appear twice bc. users can re-do earn without gaining an advantage. Filter by the hash h / ecdsa signature
    private final List<EarnTransactionData> earnTransactionDataList = new ArrayList<>();
    private final Map<Zn.ZnElement,List<SpendTransactionData>> spendTransactionDataList = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, String> doubleSpendingDetected = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void addEarnData(EarnTransactionData earnTransactionData) {
        earnTransactionDataList.add(earnTransactionData);
    }

    @Override
    public void addSpendData(SpendTransactionData spendTransactionData) {
        spendTransactionDataList.putIfAbsent(spendTransactionData.getDsid(), Collections.synchronizedList(new ArrayList<>()));
        var entriesForDsid = spendTransactionDataList.get(spendTransactionData.getDsid());
        entriesForDsid.add(spendTransactionData);
    }

    // Start after one minute, sync every minute
    @Scheduled(initialDelay = 60_000, fixedRate = 60_000)
    public void linkAll() {
        spendTransactionDataList.forEach((znElement, spendTransactionDataList) -> {
            var first = spendTransactionDataList.get(0);
            var secondOptional = spendTransactionDataList.stream().filter(entry -> !entry.getC().equals(first.getC())).findAny();
            if (secondOptional.isPresent()) {
                var second = secondOptional.get();
                doubleSpendingDetected.put(first.getBasketId(), "TODO");
                doubleSpendingDetected.put(second.getBasketId(), "TODO");
            }
        });
    }

    public Map<UUID, String> getDoubleSpendingDetected() {
        return Collections.unmodifiableMap(doubleSpendingDetected);
    }

    public Map<Zn.ZnElement, List<SpendTransactionData>> getSpendTransactionDataList() {
        return Collections.unmodifiableMap(spendTransactionDataList);
    }
}
