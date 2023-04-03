package org.cryptimeleon.incentive.services.store.storage;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BasketRepository extends CrudRepository<BasketEntity, UUID> {
}
