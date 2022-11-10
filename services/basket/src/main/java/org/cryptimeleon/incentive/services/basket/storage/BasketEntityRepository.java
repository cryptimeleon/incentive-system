package org.cryptimeleon.incentive.services.basket.storage;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface BasketEntityRepository extends CrudRepository<BasketEntity, UUID> {
}
