package kboyle.degenerate.commands.modules;

import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.services.PrefixService;
import kboyle.oktane.reactive.module.annotations.Aliases;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

@Aliases({"prefix", "p"})
public class PrefixManagement extends DegenerateModule {
    private final PrefixService prefixService;

    public PrefixManagement(PrefixService prefixService) {
        this.prefixService = prefixService;
    }

    @Aliases({"add", "a"})
    public Mono<CommandResult> addPrefix(String prefix) {
        return context().guild()
            .flatMap(guild -> {
                var result = prefixService.addPrefix(guild.getId(), prefix);

                if (result) {
                    return embed("Added prefix " + prefix);
                }

                return embed("Prefix was already added");
            });
    }

    @Aliases({"remove", "r", "rm"})
    public Mono<CommandResult> removePrefix(String prefix) {
        return context().guild()
            .flatMap(guild -> {
                var result = prefixService.removePrefix(guild.getId(), prefix);

                if (result) {
                    return embed("Removed prefix " + prefix);
                }

                return embed("Prefix wasn't already added");
            });
    }

    @Aliases({"list", "l", "ls"})
    public Mono<CommandResult> listPrefixes() {
        return context().guild()
            .flatMap(guild -> {
                var prefixes = prefixService.getPrefixes(guild.getId());

                if (prefixes.isEmpty()) {
                    return embed("No prefixes");
                }

                return embed(String.join(", ", prefixes));
            });
    }
}
