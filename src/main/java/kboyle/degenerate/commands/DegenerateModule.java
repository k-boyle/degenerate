package kboyle.degenerate.commands;

import discord4j.core.spec.MessageCreateSpec;
import kboyle.degenerate.commands.results.DiscordCreateMessageResult;
import kboyle.degenerate.commands.results.DiscordEmbedResult;
import kboyle.degenerate.commands.results.DiscordReplyResult;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

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

    protected Mono<CommandResult> createMessage(Consumer<MessageCreateSpec> specConsumer) {
        return Mono.just(new DiscordCreateMessageResult(context().command(), context().message.getId(), context().channel, specConsumer));
    }
}
