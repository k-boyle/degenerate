package kboyle.degenerate.services;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
import kboyle.oktane.core.prefix.Prefix;
import kboyle.oktane.discord4j.prefix.MentionPrefix;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Component
public class PrefixService {
    private final PersistedGuildRepository repo;
    private final ConcurrentHashMap<Snowflake, Set<Prefix>> prefixesByGuildId;

    public PrefixService(PersistedGuildRepository repo) {
        this.repo = repo;
        this.prefixesByGuildId = new ConcurrentHashMap<>();
    }

    public boolean addPrefix(Guild guild, Prefix prefix) {
        return update(guild, prefix, Set::add);
    }

    public boolean removePrefix(Guild guild, Prefix prefix) {
        return update(guild, prefix, Set::remove);
    }

    private boolean update(Guild guild, Prefix prefix, BiFunction<Set<Prefix>, Prefix, Boolean> func) {
        var persistedGuild = repo.get(guild);
        boolean updated = func.apply(persistedGuild.getPrefixes(), prefix);

        if (updated) {
            prefixesByGuildId.put(guild.getId(), persistedGuild.getPrefixes());
            repo.save(persistedGuild);
        }

        return updated;
    }

    public Set<Prefix> getPrefixes(Guild guild) {
        var copy = new HashSet<>(getOrFetchPrefixes(guild));
        copy.add(MentionPrefix.bot());
        return copy;
    }

    private Set<Prefix> getOrFetchPrefixes(Guild guild) {
        return prefixesByGuildId.compute(guild.getId(), (id, prefixes) -> {
            if (prefixes == null) {
                return repo.get(guild).getPrefixes();
            }

            return prefixes;
        });
    }
}
