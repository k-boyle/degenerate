package kboyle.degenerate.persistence.dao;

import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import org.springframework.data.repository.CrudRepository;

public interface PersistedRssFeedRepository extends CrudRepository<PersistedRssFeed, String> {
}
