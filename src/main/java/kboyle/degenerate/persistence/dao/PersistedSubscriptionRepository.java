package kboyle.degenerate.persistence.dao;

import kboyle.degenerate.persistence.entities.PersistedFeedSubscription;
import org.springframework.data.repository.CrudRepository;

public interface PersistedSubscriptionRepository extends CrudRepository<PersistedFeedSubscription, Long> {
}
