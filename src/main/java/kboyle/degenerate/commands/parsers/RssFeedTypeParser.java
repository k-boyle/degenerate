package kboyle.degenerate.commands.parsers;

import kboyle.degenerate.commands.DegenerateContext;
import kboyle.degenerate.persistence.dao.PersistedRssFeedRepository;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

public class RssFeedTypeParser extends DegenerateTypeParser<PersistedRssFeed> {
    @Override
    public Mono<TypeParserResult<PersistedRssFeed>> parse(DegenerateContext context, Command command, String input) {
        return context.beanProvider().getBean(PersistedRssFeedRepository.class)
            .findById(input)
            .map(this::success)
            .orElseGet(() -> failure("Failed to find a feed with name %s", input))
            .mono();
    }
}
