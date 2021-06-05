package kboyle.degenerate.commands.parsers;

import com.github.redouane59.twitter.TwitterClient;
import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
import kboyle.degenerate.persistence.entities.PersistedTwitterSubscription;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.parsers.DiscordTypeParser;
import reactor.core.publisher.Mono;

public class TwitterSubscriptionTypeParser extends DiscordTypeParser<PersistedTwitterSubscription> {
    @Override
    public Mono<TypeParserResult<PersistedTwitterSubscription>> parse(DiscordCommandContext context, Command command, String input) {
        return context.guild()
            .map(guild -> {
                var repo = context.beanProvider().getBean(PersistedGuildRepository.class);
                var persistedGuild = repo.get(guild);
                return persistedGuild.getTwitterSubscriptions().stream()
                    .filter(subscription -> {
                        var userId = subscription.getId();
                        if (userId.equals(input)) {
                            return true;
                        }

                        var client = context.beanProvider().getBean(TwitterClient.class);
                        var user = client.getUserFromUserName(input);
                        return user.getName().equalsIgnoreCase(input) || user.getDisplayedName().equalsIgnoreCase(input);
                    })
                    .findFirst()
                    .map(this::success)
                    .orElseGet(() -> failure("Failed to get a Twitter subscription for input %s", input));
            });
    }
}
