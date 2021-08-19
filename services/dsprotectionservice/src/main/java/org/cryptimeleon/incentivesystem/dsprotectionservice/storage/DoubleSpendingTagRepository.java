package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoubleSpendingTagRepository extends CrudRepository<DoubleSpendingTag, Long> {}
