package kboyle.degenerate.commands.parsers;

import kboyle.degenerate.commands.DegenerateContext;
import kboyle.degenerate.persistence.dao.PersistedRssFeedRepository;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

public class RssFeedTypeParser extends DegenerateTypeParser<PersistedRssFeed> {
    @Override
    public Mono<TypeParserResult<PersistedRssFeed>> parse(DegenerateContext context, ReactiveCommand command, String input) {
        return context.beanProvider().getBean(PersistedRssFeedRepository.class)
            .findById(input)
            .map(this::monoSuccess)
            .orElseGet(() -> monoFailure("Failed to find a feed with url %s", input));
    }
}
