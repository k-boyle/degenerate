package kboyle.degenerate.commands.modules;

import discord4j.rest.util.Permission;
import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.services.PrefixService;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.module.annotations.Description;
import kboyle.oktane.core.module.annotations.Name;
import kboyle.oktane.core.prefix.StringPrefix;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.discord4j.precondition.PermissionTarget;
import kboyle.oktane.discord4j.precondition.RequirePermission;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Aliases({"prefix", "p"})
@Description("Mange the prefixes for your guild")
@Name("Prefix Management")
public class PrefixModule extends DegenerateModule {
    private final PrefixService prefixService;

    public PrefixModule(PrefixService prefixService) {
        this.prefixService = prefixService;
    }

    @Aliases({"add", "a"})
    @RequirePermission(target = PermissionTarget.USER, permissions = Permission.ADMINISTRATOR)
    public Mono<CommandResult> addPrefix(String prefix) {
        return context().guild()
            .map(guild -> {
                var result = prefixService.addPrefix(guild, new StringPrefix(prefix));

                if (result) {
                    return embed("Added prefix " + prefix);
                }

                return embed("Prefix was already added");
            });
    }

    @Aliases({"remove", "r", "rm"})
    @RequirePermission(target = PermissionTarget.USER, permissions = Permission.ADMINISTRATOR)
    public Mono<CommandResult> removePrefix(String prefix) {
        return context().guild()
            .map(guild -> {
                var result = prefixService.removePrefix(guild, new StringPrefix(prefix));

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

                var formattedPrefixes = prefixes.stream()
                    .map(prefix -> prefix.value().toString())
                    .collect(Collectors.joining(", "));
                return embed(formattedPrefixes);
            });
    }
}
