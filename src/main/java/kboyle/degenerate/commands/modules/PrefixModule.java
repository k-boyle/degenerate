package kboyle.degenerate.commands.modules;

import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.commands.preconditions.RequireUserPermission;
import kboyle.degenerate.services.PrefixService;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.module.annotations.Description;
import kboyle.oktane.core.module.annotations.Name;
import kboyle.oktane.core.module.annotations.Require;
import kboyle.oktane.core.processor.OktaneModule;
import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

@Aliases({"prefix", "p"})
@Description("Mange the prefixes for your guild")
@Name("Prefix Management")
@OktaneModule
public class PrefixModule extends DegenerateModule {
    private final PrefixService prefixService;

    public PrefixModule(PrefixService prefixService) {
        this.prefixService = prefixService;
    }

    @Aliases({"add", "a"})
    @Require(precondition = RequireUserPermission.class, arguments = "ADMINISTRATOR")
    public Mono<CommandResult> addPrefix(String prefix) {
        return context().guild()
            .map(guild -> {
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
            .map(guild -> {
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
            .map(guild -> {
                var prefixes = prefixService.getPrefixes(guild);

                if (prefixes.isEmpty()) {
                    return embed("No prefixes");
                }

                return embed(String.join(", ", prefixes));
            });
    }
}
