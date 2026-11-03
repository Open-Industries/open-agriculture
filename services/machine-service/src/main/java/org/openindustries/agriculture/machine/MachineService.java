package org.openindustries.agriculture.machine;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MachineService {

    @Inject
    MachineRepository repository;

    public Machine create(CreateMachineRequest request) {
        return repository.save(Machine.create(request.name(), request.machineType(), request.powerKw()));
    }

    public Machine get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException(404, "Machine not found: " + id));
    }

    public List<Machine> list() {
        return repository.findAll();
    }

    public Machine deploy(UUID id) {
        Machine entity = get(id);
        if (!"AVAILABLE".equals(entity.status)) {
            throw new DomainException(409, "Only a AVAILABLE machine can transition via deploy");
        }
        entity.status = "DEPLOYED";
        return repository.save(entity);
    }
}
