package org.openindustries.agriculture.field;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FieldService {

    @Inject
    FieldRepository repository;

    public Field create(CreateFieldRequest request) {
        return repository.save(Field.create(request.name(), request.cropType(), request.hectares(), request.regionCode()));
    }

    public Field get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException(404, "Field not found: " + id));
    }

    public List<Field> list() {
        return repository.findAll();
    }

    public Field fallow(UUID id) {
        Field entity = get(id);
        if ("FALLOW".equals(entity.status)) {
            throw new DomainException(409, "Field already FALLOW");
        }
        entity.status = "FALLOW";
        return repository.save(entity);
    }

    public Field reactivate(UUID id) {
        Field entity = get(id);
        if ("ACTIVE".equals(entity.status)) {
            throw new DomainException(409, "Field already ACTIVE");
        }
        entity.status = "ACTIVE";
        return repository.save(entity);
    }
}
