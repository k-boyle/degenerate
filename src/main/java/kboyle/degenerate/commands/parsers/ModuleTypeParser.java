package kboyle.degenerate.commands.parsers;

import kboyle.degenerate.commands.DegenerateContext;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

import static kboyle.degenerate.Utils.insensitiveContains;

public class ModuleTypeParser extends DegenerateTypeParser<CommandModule> {
    @SuppressWarnings("unchecked")
    @Override
    public Mono<TypeParserResult<CommandModule>> parse(DegenerateContext context, Command command, String input) {
        var handler = (CommandHandler<DegenerateContext>) context.beanProvider().getBean(CommandHandler.class);
        return handler.flattenModules()
            .filter(module -> matchingModule(input, module))
            .findFirst()
            .map(this::success)
            .orElseGet(() -> failure("Failed to find a module matching input %s", input))
            .mono();
    }

    private boolean matchingModule(String input, CommandModule module) {
        return insensitiveContains(module.name, input) || module.groups.stream().anyMatch(group -> insensitiveContains(group, input));
    }
}
