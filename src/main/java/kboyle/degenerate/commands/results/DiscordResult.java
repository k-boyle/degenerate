package kboyle.degenerate.commands.results;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.SuccessfulResult;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public abstract class DiscordResult implements CommandResult, SuccessfulResult {
    private final ReactiveCommand command;
    private final TextChannel channel;

    protected DiscordResult(ReactiveCommand command, TextChannel channel) {
        this.command = command;
        this.channel = channel;
    }

    @Override
    public ReactiveCommand command() {
        return command;
    }

    public abstract void applySpec(MessageCreateSpec spec);

    public Mono<Message> sendMessage() {
        return channel.createMessage(this::applySpec);
    }
}
