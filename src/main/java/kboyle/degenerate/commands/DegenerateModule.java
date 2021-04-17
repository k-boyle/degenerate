package kboyle.degenerate.commands;

import kboyle.degenerate.commands.results.DiscordEmbedResult;
import kboyle.degenerate.commands.results.DiscordReplyResult;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public abstract class DegenerateModule extends ReactiveModuleBase<DegenerateContext> {
    protected Mono<CommandResult> reply(String content) {
        return Mono.just(new DiscordReplyResult(context().command(), context().channel, content, context().message.getId()));
    }

    protected Mono<CommandResult> embed(String content) {
        return Mono.just(new DiscordEmbedResult(context().command(), context().channel, content, context().message.getId()));
    }

    protected Mono<CommandResult> embed(String content, Object... args) {
        return embed(String.format(content, args));
    }
}
