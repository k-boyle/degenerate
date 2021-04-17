package kboyle.degenerate.persistence.dao;

import discord4j.core.object.entity.Guild;
import kboyle.degenerate.persistence.entities.PersistedGuild;

import java.util.HashMap;
import java.util.HashSet;

public interface PersistedGuildRepository extends LazyCrudRepository<PersistedGuild, Long> {
   default PersistedGuild createNew(Long id) {
       return new PersistedGuild(id, new HashSet<>(), new HashMap<>());
   }

   default PersistedGuild get(Guild guild) {
       return get(guild.getId().asLong());
   }
}
