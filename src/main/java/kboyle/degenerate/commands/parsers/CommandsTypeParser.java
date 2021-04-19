package kboyle.degenerate.commands.parsers;

import kboyle.degenerate.commands.DegenerateContext;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.ReactiveCommandHandler;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.parsers.ReactiveTypeParser;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static kboyle.degenerate.Utils.insensitiveContains;

public class CommandsTypeParser implements ReactiveTypeParser<List<ReactiveCommand>> {
    @SuppressWarnings("unchecked")
    @Override
    public Mono<TypeParserResult<List<ReactiveCommand>>> parse(CommandContext context, ReactiveCommand c, String input) {
        var handler = (ReactiveCommandHandler<DegenerateContext>) context.beanProvider().getBean(ReactiveCommandHandler.class);
        var commands = handler.commands()
            .filter(command -> matchingCommand(input, command))
            .collect(Collectors.toList());

        return commands.isEmpty()
            ? monoFailure("Failed to find any commands matching input %s", input)
            : monoSuccess(commands);
    }

    private boolean matchingCommand(String input, ReactiveCommand command) {
        return insensitiveContains(command.name, input) || command.aliases.stream().anyMatch(alias -> insensitiveContains(alias, input));
    }
}
