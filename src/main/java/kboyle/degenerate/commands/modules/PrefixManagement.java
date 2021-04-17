package kboyle.degenerate.commands.modules;

import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.commands.preconditions.RequireUserPermission;
import kboyle.degenerate.services.PrefixService;
import kboyle.oktane.reactive.module.annotations.Aliases;
import kboyle.oktane.reactive.module.annotations.Description;
import kboyle.oktane.reactive.module.annotations.Require;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

@Aliases({"prefix", "p"})
@Description("Mange the prefixes for your guild")
public class PrefixManagement extends DegenerateModule {
    private final PrefixService prefixService;

    public PrefixManagement(PrefixService prefixService) {
        this.prefixService = prefixService;
    }

    @Aliases({"add", "a"})
    @Require(precondition = RequireUserPermission.class, arguments = "ADMINISTRATOR")
    public Mono<CommandResult> addPrefix(String prefix) {
        return context().guild()
            .flatMap(guild -> {
                var result = prefixService.addPrefix(guild, prefix);

                if (result) {
                    return embed("Added prefix " + prefix);
                }

                return embed("Prefix was already added");
            });
    }

    @Aliases({"remove", "r", "rm"})
    @Require(precondition = RequireUserPermission.class, arguments = "ADMINISTRATOR")
    public Mono<CommandResult> removePrefix(String prefix) {
        return context().guild()
            .flatMap(guild -> {
                var result = prefixService.removePrefix(guild, prefix);

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
                var prefixes = prefixService.getPrefixes(guild);

                if (prefixes.isEmpty()) {
                    return embed("No prefixes");
                }

                return embed(String.join(", ", prefixes));
            });
    }
}
