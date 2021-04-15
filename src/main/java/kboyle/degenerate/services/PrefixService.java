package kboyle.degenerate.services;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.entity.Message;
import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
import kboyle.degenerate.persistence.entities.PersistedGuild;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class PrefixService {
    private final PersistedGuildRepository repo;
    private final ApplicationInfo applicationInfo;
    private final ConcurrentHashMap<Snowflake, Set<String>> prefixesByGuildId;

    public PrefixService(PersistedGuildRepository repo, ApplicationInfo applicationInfo) {
        this.repo = repo;
        this.applicationInfo = applicationInfo;
        this.prefixesByGuildId = new ConcurrentHashMap<>();
    }

    public Mono<String> hasPrefix(Message message) {
        String content = message.getContent();

        if (content.isEmpty()) {
            return Mono.empty();
        }

        if (content.length() > 21 && content.charAt(0) == '<' && content.charAt(1) == '@') {
            int start = 2;
            if (content.charAt(start) == '!') {
                start++;
            }

            int end = content.indexOf('>');

            if (end != -1) {
                String strId = content.substring(start, end);
                try {
                    long id = Long.parseLong(strId);
                    if (id == applicationInfo.getId().asLong()) {
                        return Mono.just(content.substring(end + 1));
                    }
                } catch (NumberFormatException ignore) {
                }
            }
        }

        return message.getGuild()
            .handle((guild, sink) -> {
                Set<String> prefixes = getOrFetchPrefixes(guild.getId());
                for (String prefix : prefixes) {
                    if (content.startsWith(prefix)) {
                        sink.next(content.substring(prefix.length()));
                    }
                }
            });
    }

    public boolean addPrefix(Snowflake guildId, String prefix) {
        return update(guildId, prefix, Set::add);
    }

    public boolean removePrefix(Snowflake guildId, String prefix) {
        return update(guildId, prefix, Set::remove);
    }

    private boolean update(Snowflake guildId, String prefix, BiFunction<Set<String>, String, Boolean> func) {
        PersistedGuild guild = repo.get(guildId.asLong());
        boolean updated = func.apply(guild.getPrefixes(), prefix);

        if (updated) {
            prefixesByGuildId.put(guildId, guild.getPrefixes());
            repo.save(guild);
        }

        return updated;
    }

    public Set<String> getPrefixes(Snowflake guildId) {
        return Set.copyOf(getOrFetchPrefixes(guildId));
    }

    private Set<String> getOrFetchPrefixes(Snowflake guildId) {
        return prefixesByGuildId.compute(guildId, (id, prefixes) -> {
            if (prefixes == null) {
                return repo.get(id.asLong()).getPrefixes();
            }

            return prefixes;
        });
    }
}
