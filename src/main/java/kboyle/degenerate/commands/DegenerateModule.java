package kboyle.degenerate.commands;

import kboyle.degenerate.commands.results.DiscordEmbedResult;
import kboyle.degenerate.commands.results.DiscordReplyResult;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public abstract class DegenerateModule extends ReactiveModuleBase<DegenerateContext> {
    protected Mono<CommandResult> reply(String content) {
        return context().channel()
            .map(channel -> new DiscordReplyResult(context().command(), channel, content, context().message.getId()));
    }

    protected Mono<CommandResult> embed(String content) {
        return context().channel()
            .map(channel -> new DiscordEmbedResult(context().command(), channel, content, context().message.getId()));
    }
}
