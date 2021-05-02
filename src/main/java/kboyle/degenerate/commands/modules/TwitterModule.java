package kboyle.degenerate.commands.modules;

import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.dto.tweet.Tweet;
import com.github.redouane59.twitter.dto.user.UserV2;
import discord4j.core.object.entity.channel.TextChannel;
import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.commands.preconditions.RequireBotOwner;
import kboyle.degenerate.commands.preconditions.RequireUserPermission;
import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
import kboyle.degenerate.persistence.dao.PersistedTwitterSubscriptionRepository;
import kboyle.degenerate.persistence.entities.PersistedTwitterSubscription;
import kboyle.oktane.core.module.annotations.*;
import kboyle.oktane.core.processor.OktaneModule;
import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Aliases("twitter")
@Name("Twitter")
@Description("Commands to manage twitter subscriptions")
@OktaneModule
public class TwitterModule extends DegenerateModule {
    private static final int TWEET_HISTORY = 10;

    private final TwitterClient twitterClient;
    private final PersistedGuildRepository guildRepo;
    private final PersistedTwitterSubscriptionRepository subscriptionRepo;

    public TwitterModule(
            TwitterClient twitterClient,
            PersistedGuildRepository guildRepo,
            PersistedTwitterSubscriptionRepository subscriptionRepo) {
        this.twitterClient = twitterClient;
        this.guildRepo = guildRepo;
        this.subscriptionRepo = subscriptionRepo;
    }

    @Aliases({"subscribe", "sub"})
    @RequireAny({
        @Require(precondition = RequireUserPermission.class, arguments = "ADMINISTRATOR"),
        @Require(precondition = RequireBotOwner.class)
    })
    public Mono<CommandResult> subscribe(@Remainder UserV2 user) {
        return subscribe(context().channel, user);
    }

    @Aliases({"subscribe", "sub"})
    @RequireAny({
        @Require(precondition = RequireUserPermission.class, arguments = "ADMINISTRATOR"),
        @Require(precondition = RequireBotOwner.class)
    })
    public Mono<CommandResult> subscribe(TextChannel channel, @Remainder UserV2 user) {
        return context().guild()
            .map(guild -> {
                var persistedGuild = guildRepo.get(guild);

                var twitterSubscriptions = persistedGuild.getTwitterSubscriptions();
                var ids = twitterClient.getUserTimeline(user.getId(), TWEET_HISTORY)
                    .stream()
                    .map(Tweet::getId)
                    .collect(Collectors.toSet());

                var subscription = new PersistedTwitterSubscription(
                    user.getId(),
                    channel.getId().asLong(),
                    ids,
                    persistedGuild
                );

                var added = twitterSubscriptions.add(subscription);

                if (!added) {
                    return embed("You are already subscribed to %s", user.getDisplayedName());
                }

                subscriptionRepo.save(subscription);
                guildRepo.save(persistedGuild);

                return embed("Subscribed to Twitter user %s", user.getDisplayedName());
            });
    }
}
