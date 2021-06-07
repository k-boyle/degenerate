package kboyle.degenerate.commands.parsers;

import kboyle.degenerate.persistence.dao.PersistedRssFeedRepository;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.processor.ConfigureWith;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.parsers.DiscordTypeParser;
import reactor.core.publisher.Mono;

@ConfigureWith
public class RssFeedTypeParser extends DiscordTypeParser<PersistedRssFeed> {
    @Override
    public Mono<TypeParserResult<PersistedRssFeed>> parse(DiscordCommandContext context, Command command, String input) {
        return context.beanProvider().getBean(PersistedRssFeedRepository.class)
            .findById(input)
            .map(this::success)
            .orElseGet(() -> failure("Failed to find a feed with name %s", input))
            .mono();
    }
}
