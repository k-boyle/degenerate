package kboyle.degenerate.commands.parsers;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.processor.ConfigureWith;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.DiscordCommandHandler;
import kboyle.oktane.discord4j.parsers.DiscordTypeParser;
import reactor.core.publisher.Mono;

import static kboyle.degenerate.Utils.insensitiveContains;

@ConfigureWith
public class ModuleTypeParser extends DiscordTypeParser<CommandModule> {
    @SuppressWarnings("unchecked")
    @Override
    public Mono<TypeParserResult<CommandModule>> parse(DiscordCommandContext context, Command command, String input) {
        var handler = (DiscordCommandHandler<DiscordCommandContext>) context.beanProvider().getBean(DiscordCommandHandler.class);
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
