package kboyle.degenerate.commands.modules;

import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.commands.preconditions.RequireBotOwner;
import kboyle.oktane.reactive.module.annotations.Aliases;
import kboyle.oktane.reactive.module.annotations.Require;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public class MiscModule extends DegenerateModule {
    @Aliases("ping")
    public Mono<CommandResult> ping() {
        return reply("pong");
    }

    @Aliases("owner")
    @Require(precondition = RequireBotOwner.class)
    public Mono<CommandResult> owner() {
        return reply("ur cute");
    }
}
