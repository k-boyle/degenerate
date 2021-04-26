package kboyle.degenerate.commands.preconditions;

import kboyle.degenerate.commands.DegenerateContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

public class RequireBotOwner extends DegeneratePrecondition {
    @Override
    protected Mono<PreconditionResult> run(DegenerateContext context, Command command) {
        return context.client.getApplicationInfo()
            .flatMap(info -> context.author()
                .map(author -> {
                    if (author.getId().equals(info.getOwnerId())) {
                        return success();
                    }

                    return failure("Only the bot owner can execute this command");
                })
            );
    }
}
