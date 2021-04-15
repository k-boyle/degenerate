package kboyle.degenerate.persistence.dao;

import kboyle.degenerate.persistence.entities.PersistedGuild;

import java.util.HashSet;

public interface PersistedGuildRepository extends LazyCrudRepository<PersistedGuild, Long> {
   default PersistedGuild createNew(Long id) {
       return new PersistedGuild(id, new HashSet<>());
   }
}
