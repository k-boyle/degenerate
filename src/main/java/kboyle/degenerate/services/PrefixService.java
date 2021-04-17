package kboyle.degenerate.services;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
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
        var content = message.getContent();

        if (content.isEmpty()) {
            return Mono.empty();
        }

        if (content.length() > 16 && content.charAt(0) == '<' && content.charAt(1) == '@') {
            var start = 2;
            if (content.charAt(start) == '!') {
                start++;
            }

            var end = content.indexOf('>');

            if (end != -1) {
                var strId = content.substring(start, end);
                try {
                    var id = Long.parseLong(strId);
                    if (id == applicationInfo.getId().asLong()) {
                        return Mono.just(content.substring(end + 1));
                    }
                } catch (NumberFormatException ignore) {
                }
            }
        }

        return message.getGuild()
            .handle((guild, sink) -> {
                var prefixes = getOrFetchPrefixes(guild);
                for (var prefix : prefixes) {
                    if (content.startsWith(prefix)) {
                        sink.next(content.substring(prefix.length()));
                    }
                }
            });
    }

    public boolean addPrefix(Guild guild, String prefix) {
        return update(guild, prefix, Set::add);
    }

    public boolean removePrefix(Guild guild, String prefix) {
        return update(guild, prefix, Set::remove);
    }

    private boolean update(Guild guild, String prefix, BiFunction<Set<String>, String, Boolean> func) {
        var persistedGuild = repo.get(guild);
        boolean updated = func.apply(persistedGuild.getPrefixes(), prefix);

        if (updated) {
            prefixesByGuildId.put(guild.getId(), persistedGuild.getPrefixes());
            repo.save(persistedGuild);
        }

        return updated;
    }

    public Set<String> getPrefixes(Guild guild) {
        return Set.copyOf(getOrFetchPrefixes(guild));
    }

    private Set<String> getOrFetchPrefixes(Guild guild) {
        return prefixesByGuildId.compute(guild.getId(), (id, prefixes) -> {
            if (prefixes == null) {
                return repo.get(guild).getPrefixes();
            }

            return prefixes;
        });
    }
}
