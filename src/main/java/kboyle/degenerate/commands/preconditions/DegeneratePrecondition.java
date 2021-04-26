package kboyle.degenerate.commands.preconditions;

import kboyle.degenerate.commands.DegenerateContext;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

public abstract class DegeneratePrecondition implements Precondition {
    @Override
    public Mono<PreconditionResult> run(CommandContext context, Command command) {
        return run((DegenerateContext) context, command);
    }

    protected abstract Mono<PreconditionResult> run(DegenerateContext context, Command command);
}
