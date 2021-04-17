package kboyle.degenerate.commands.parsers;

import kboyle.degenerate.commands.DegenerateContext;
import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
import kboyle.degenerate.persistence.entities.PersistedFeedSubscription;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.FailedResult;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

public class RssFeedSubscriptionTypeParser extends DegenerateTypeParser<PersistedFeedSubscription> {
    private final RssFeedTypeParser feedTypeParser;

    public RssFeedSubscriptionTypeParser() {
        feedTypeParser = new RssFeedTypeParser();
    }

    @Override
    public Mono<TypeParserResult<PersistedFeedSubscription>> parse(DegenerateContext context, ReactiveCommand command, String input) {
        return feedTypeParser.parse(context, command, input)
            .flatMap(result -> {
                if (!result.success()) {
                    return monoFailure(((FailedResult) result).reason());
                }

                return context.guild()
                    .map(guild -> {
                        var feed = result.value();
                        var repo = context.beanProvider().getBean(PersistedGuildRepository.class);
                        var persistedGuild = repo.get(guild.getId().asLong());
                        var persistedFeedSubscription = persistedGuild.getSubscriptionByFeedUrl().get(feed);

                        if (persistedFeedSubscription != null) {
                            return success(persistedFeedSubscription);
                        }

                        return failure("");
                    });
            });
    }
}
