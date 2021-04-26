package kboyle.degenerate.commands.results;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.SuccessfulResult;
import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

public abstract class DiscordResult implements CommandResult, SuccessfulResult {
    private final Command command;

    protected DiscordResult(Command command) {
        this.command = command;
    }

    @Override
    public Command command() {
        return command;
    }

    public abstract Mono<Void> execute();
}
