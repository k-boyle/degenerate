package kboyle.degenerate.persistence.dao;

import kboyle.degenerate.persistence.entities.PersistedRssFeed;

import java.util.HashSet;

public interface PersistedRssFeedRepository extends LazyCrudRepository<PersistedRssFeed, String> {
    @Override
    default PersistedRssFeed createNew(String url) {
        return new PersistedRssFeed(url, new HashSet<>());
    }
}
