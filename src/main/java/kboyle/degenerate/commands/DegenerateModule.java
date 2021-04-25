package kboyle.degenerate.commands;

import discord4j.core.spec.MessageCreateSpec;
import kboyle.degenerate.commands.results.DiscordCreateMessageResult;
import kboyle.degenerate.commands.results.DiscordEmbedResult;
import kboyle.degenerate.commands.results.DiscordReplyResult;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.results.command.CommandResult;

import java.util.function.Consumer;

public abstract class DegenerateModule extends ReactiveModuleBase<DegenerateContext> {
    protected CommandResult reply(String content) {
        return new DiscordReplyResult(context().command(), context().channel, content, context().message.getId());
    }

    protected CommandResult embed(String content) {
        return new DiscordEmbedResult(context().command(), context().channel, content, context().message.getId());
    }

    protected CommandResult embed(String content, Object... args) {
        return embed(String.format(content, args));
    }

    protected CommandResult createMessage(Consumer<MessageCreateSpec> specConsumer) {
        return new DiscordCreateMessageResult(context().command(), context().message.getId(), context().channel, specConsumer);
    }
}
