package kboyle.degenerate.persistence.dao;

import kboyle.degenerate.persistence.entities.PersistedTwitterSubscription;
import org.springframework.data.repository.CrudRepository;

public interface PersistedTwitterSubscriptionRepository extends CrudRepository<PersistedTwitterSubscription, String> {
}
