package kboyle.degenerate.commands.results;

import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.SuccessfulResult;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public abstract class DiscordResult implements CommandResult, SuccessfulResult {
    private final ReactiveCommand command;

    protected DiscordResult(ReactiveCommand command) {
        this.command = command;
    }

    @Override
    public ReactiveCommand command() {
        return command;
    }

    public abstract Mono<Void> execute();
}
