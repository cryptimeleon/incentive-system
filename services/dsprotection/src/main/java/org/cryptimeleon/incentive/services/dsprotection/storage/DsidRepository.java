package org.cryptimeleon.incentive.services.dsprotection.storage;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DsidRepository extends CrudRepository<DsIdEntry, Long> {
}
