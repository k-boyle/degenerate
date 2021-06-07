package kboyle.degenerate.commands.parsers;

import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
import kboyle.degenerate.persistence.entities.PersistedFeedSubscription;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.processor.ConfigureWith;
import kboyle.oktane.core.results.FailedResult;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.parsers.DiscordTypeParser;
import reactor.core.publisher.Mono;

@ConfigureWith
public class RssFeedSubscriptionTypeParser extends DiscordTypeParser<PersistedFeedSubscription> {
    private final RssFeedTypeParser feedTypeParser;

    public RssFeedSubscriptionTypeParser() {
        feedTypeParser = new RssFeedTypeParser();
    }

    @Override
    public Mono<TypeParserResult<PersistedFeedSubscription>> parse(DiscordCommandContext context, Command command, String input) {
        return feedTypeParser.parse(context, command, input)
            .flatMap(result -> {
                if (!result.success()) {
                    return failure(((FailedResult) result).reason()).mono();
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

                        return failure("This guild isn't subscribed to %s", feed.getName());
                    });
            });
    }
}
