package kboyle.degenerate.commands.preconditions;

import kboyle.degenerate.commands.DegenerateContext;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.module.ReactivePrecondition;
import kboyle.oktane.reactive.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

public abstract class DegeneratePrecondition implements ReactivePrecondition {
    @Override
    public Mono<PreconditionResult> run(CommandContext context, ReactiveCommand command) {
        return run((DegenerateContext) context, command);
    }

    protected abstract Mono<PreconditionResult> run(DegenerateContext context, ReactiveCommand command);
}
