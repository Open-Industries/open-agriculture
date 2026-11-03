package org.openindustries.agriculture.irrigation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class IrrigationService {

    @Inject
    IrrigationRepository repository;

    public WaterBudget open(OpenWaterBudgetRequest request) {
        return repository.saveAccount(WaterBudget.open(request.fieldId(), request.waterUnits()));
    }

    public WaterBudget get(UUID id) {
        return repository.findAccount(id)
                .orElseThrow(() -> new DomainException(404, "WaterBudget not found: " + id));
    }

    /**
     * Draws from the account. Invariant: the balance/capacity never goes negative —
     * an over-drawing operation is rejected (422) and nothing is recorded.
     */
    public WaterUse consume(UUID accountId, CreateWaterUseRequest request) {
        WaterBudget account = get(accountId);
        if (!"ACTIVE".equals(account.status)) {
            throw new DomainException(409, "WaterBudget is not ACTIVE");
        }
        if (request.units() > account.waterUnits) {
            throw new DomainException(422, "Insufficient capacity: requested " + request.units()
                    + " exceeds available " + account.waterUnits);
        }
        account.waterUnits -= request.units();
        repository.saveAccount(account);
        return repository.saveRecord(WaterUse.of(accountId, request.useType(), request.units(), account.waterUnits));
    }

    public List<WaterUse> recordsOf(UUID accountId) {
        get(accountId);
        return repository.recordsOf(accountId);
    }
}
