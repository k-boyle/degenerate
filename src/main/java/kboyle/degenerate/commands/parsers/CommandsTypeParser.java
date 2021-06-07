package kboyle.degenerate.commands.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.parsers.TypeParser;
import kboyle.oktane.core.processor.ConfigureWith;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.DiscordCommandHandler;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static kboyle.degenerate.Utils.insensitiveContains;

@ConfigureWith
public class CommandsTypeParser implements TypeParser<List<Command>> {
    @SuppressWarnings("unchecked")
    @Override
    public Mono<TypeParserResult<List<Command>>> parse(CommandContext context, Command c, String input) {
        var handler = (DiscordCommandHandler<DiscordCommandContext>) context.beanProvider().getBean(DiscordCommandHandler.class);
        var commands = handler.commands()
            .filter(command -> matchingCommand(input, command))
            .collect(Collectors.toList());

        var result = commands.isEmpty()
            ? failure("Failed to find any commands matching input %s", input)
            : success(commands);

        return result.mono();
    }

    private boolean matchingCommand(String input, Command command) {
        return insensitiveContains(command.name, input) || command.aliases.stream().anyMatch(alias -> insensitiveContains(alias, input));
    }
}
