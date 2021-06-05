package kboyle.degenerate.commands.modules;

import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.dto.tweet.Tweet;
import com.github.redouane59.twitter.dto.user.UserV2;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.Permission;
import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
import kboyle.degenerate.persistence.entities.PersistedTwitterSubscription;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.module.annotations.Description;
import kboyle.oktane.core.module.annotations.Name;
import kboyle.oktane.core.module.annotations.Remainder;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.discord4j.precondition.PermissionTarget;
import kboyle.oktane.discord4j.precondition.RequireBotOwner;
import kboyle.oktane.discord4j.precondition.RequirePermission;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Aliases("twitter")
@Name("Twitter")
@Description("Commands to manage twitter subscriptions")
public class TwitterModule extends DegenerateModule {
    private static final int TWEET_HISTORY = 10;

    private final TwitterClient twitterClient;
    private final PersistedGuildRepository guildRepo;

    public TwitterModule(TwitterClient twitterClient, PersistedGuildRepository guildRepo) {
        this.twitterClient = twitterClient;
        this.guildRepo = guildRepo;
    }

    @Aliases({"subscribe", "sub"})
    @RequireBotOwner(group = "owner or admin")
    @RequirePermission(target = PermissionTarget.USER, permissions = Permission.ADMINISTRATOR, group = "owner or admin")
    public Mono<CommandResult> subscribe(@Remainder UserV2 user) {
        return context().channel().ofType(TextChannel.class).flatMap(channel -> subscribe(channel, user));
    }

    @Aliases({"subscribe", "sub"})
    @RequireBotOwner(group = "owner or admin")
    @RequirePermission(target = PermissionTarget.USER, permissions = Permission.ADMINISTRATOR, group = "owner or admin")
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

                guildRepo.save(persistedGuild);

                return embed("Subscribed to Twitter user %s", user.getDisplayedName());
            });
    }

    @Aliases({"unsubscribe", "unsub"})
    @RequireBotOwner(group = "owner or admin")
    @RequirePermission(target = PermissionTarget.USER, permissions = Permission.ADMINISTRATOR, group = "owner or admin")
    public Mono<CommandResult> unsubscribe(@Remainder PersistedTwitterSubscription subscription) {
        return context().guild()
            .map(guild -> {
                var persistedGuild = guildRepo.get(guild);

                if (!persistedGuild.getTwitterSubscriptions().remove(subscription)) {
                    return embed("You aren't subscribed to this user");
                }

                guildRepo.save(persistedGuild);

                return embed("You have been unsubscribed from this user");
            });
    }
}
