package org.openindustries.agriculture.irrigation;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** Outbound port; default in-memory adapter, production adapter is JPA on this service's own database. */
@ApplicationScoped
public class IrrigationRepository {
    private final ConcurrentHashMap<UUID, WaterBudget> accounts = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<WaterUse> records = new CopyOnWriteArrayList<>();

    public WaterBudget saveAccount(WaterBudget account) {
        accounts.put(account.id, account);
        return account;
    }

    public Optional<WaterBudget> findAccount(UUID id) {
        return Optional.ofNullable(accounts.get(id));
    }

    public WaterUse saveRecord(WaterUse record) {
        records.add(record);
        return record;
    }

    public List<WaterUse> recordsOf(UUID accountId) {
        return records.stream().filter(r -> r.accountId.equals(accountId)).toList();
    }
}
