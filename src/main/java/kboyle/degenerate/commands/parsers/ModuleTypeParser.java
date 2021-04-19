package kboyle.degenerate.commands.parsers;

import kboyle.degenerate.commands.DegenerateContext;
import kboyle.oktane.reactive.ReactiveCommandHandler;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.module.ReactiveModule;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

import static kboyle.degenerate.Utils.getAllModules;
import static kboyle.degenerate.Utils.insensitiveContains;

public class ModuleTypeParser extends DegenerateTypeParser<ReactiveModule> {
    @SuppressWarnings("unchecked")
    @Override
    public Mono<TypeParserResult<ReactiveModule>> parse(DegenerateContext context, ReactiveCommand command, String input) {
        var handler = (ReactiveCommandHandler<DegenerateContext>) context.beanProvider().getBean(ReactiveCommandHandler.class);
        return getAllModules(handler)
            .filter(module -> matchingModule(input, module))
            .findFirst()
            .map(this::monoSuccess)
            .orElseGet(() -> monoFailure("Failed to find a module matching input %s", input));
    }

    private boolean matchingModule(String input, ReactiveModule module) {
        return insensitiveContains(module.name, input) || module.groups.stream().anyMatch(group -> insensitiveContains(group, input));
    }
}
