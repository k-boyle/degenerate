package kboyle.degenerate.commands.modules;

import discord4j.core.object.entity.channel.TextChannel;
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

    @Aliases("channel")
    public Mono<CommandResult> channel(TextChannel channel) {
        return reply(channel.getName());
    }

    @Aliases("help")
    public Mono<CommandResult> thereIsNoHelp() {
        return embed("you're beyond helping");
    }
}
