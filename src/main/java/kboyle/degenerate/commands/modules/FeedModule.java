package kboyle.degenerate.commands.modules;

import discord4j.core.object.entity.channel.TextChannel;
import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
import kboyle.degenerate.persistence.dao.PersistedRssFeedRepository;
import kboyle.degenerate.persistence.dao.PersistedSubscriptionRepository;
import kboyle.degenerate.persistence.entities.PersistedFeedSubscription;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.oktane.reactive.module.annotations.Aliases;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.StringJoiner;

@Aliases({"feed", "f"})
public class FeedModule extends DegenerateModule {
    private final PersistedGuildRepository guildRepo;
    private final PersistedRssFeedRepository feedRepo;

    public FeedModule(PersistedGuildRepository guildRepo, PersistedRssFeedRepository feedRepo) {
        this.guildRepo = guildRepo;
        this.feedRepo = feedRepo;
    }

    @Aliases({"subscribe", "sub"})
    public Mono<CommandResult> subscribe(PersistedRssFeed feed) {
        return subscribe(feed, context().channel);
    }

    @Aliases({"subscribe", "sub"})
    public Mono<CommandResult> subscribe(PersistedRssFeed feed, TextChannel channel) {
        return context().guild()
            .flatMap(guild -> {
                var persistedGuild = guildRepo.get(guild);
                var subscribedFeeds = persistedGuild.getSubscriptionByFeedUrl();
                var subscription = subscribedFeeds.get(feed);

                if (subscription != null) {
                    return embed("You are already subscribed to this feed");
                }

                subscription = new PersistedFeedSubscription(
                    feed,
                    new HashSet<>(),
                    new HashSet<>(),
                    channel.getId().asLong()
                );

                subscribedFeeds.put(feed, subscription);
                guildRepo.save(persistedGuild);

                return embed("Subscribed to feed %s", feed.getUrl());
            });
    }

    @Aliases({"unsubscribe", "unsub"})
    public Mono<CommandResult> unscribed(PersistedFeedSubscription subscription) {
        return context().guild()
            .flatMap(guild -> {
                var persistedGuild = guildRepo.get(guild);
                var removed = persistedGuild.getSubscriptionByFeedUrl().remove(subscription.getFeed());

                if (removed == null) {
                    return embed("You weren't subscribed to this feed");
                }

                guildRepo.save(persistedGuild);
                return embed("Unsubscribed from feed %s", removed.getFeed().getUrl());
            });
    }

    @Aliases({"list", "l", "available"})
    public Mono<CommandResult> listFeeds() {
        var sb = new StringJoiner("\n");
        for (var feed : feedRepo.findAll()) {
            sb.add(feed.getUrl());
        }

        return embed(sb.length() == 0 ? "No available feeds" : sb.toString());
    }

    @Aliases({"subscribed", "subbed"})
    public Mono<CommandResult> subbedFeeds() {
        return context().guild()
            .flatMap(guild -> {
                var persistedGuild = guildRepo.get(guild);
                var joiner = new StringJoiner("\n");
                for (var persistedRssFeed : persistedGuild.getSubscriptionByFeedUrl().keySet()) {
                    joiner.add(persistedRssFeed.getUrl());
                }

                return joiner.length() == 0
                    ? embed("This guild isn't subscribed to any feeds")
                    : embed(joiner.toString());
            });
    }

    @Aliases({"filter", "f"})
    public static class FeedFilterModule extends DegenerateModule {
        @Aliases({"add", "a"})
        public static class FeedAddFilterModule extends DegenerateModule {
            private final PersistedSubscriptionRepository repo;

            public FeedAddFilterModule(PersistedSubscriptionRepository repo) {
                this.repo = repo;
            }

            @Aliases("title")
            public Mono<CommandResult> addTitleFilter(PersistedFeedSubscription subscription, String regex) {
                var added = subscription.getTitleRegexes().add(regex);
                if (!added) {
                    return embed("This filter had already been applied");
                }

                repo.save(subscription);
                return embed("Added filter %s", regex);
            }

            @Aliases({"description", "desc"})
            public Mono<CommandResult> addDescriptionFilter(PersistedFeedSubscription subscription, String regex) {
                var added = subscription.getDescriptionRegexes().add(regex);
                if (!added) {
                    return embed("This filter had already been applied");
                }

                repo.save(subscription);
                return embed("Added filter %s", regex);
            }
        }

        @Aliases({"remove", "rm", "r"})
        public static class FeedRemoveFilterModule extends DegenerateModule {
            private final PersistedSubscriptionRepository repo;

            public FeedRemoveFilterModule(PersistedSubscriptionRepository repo) {
                this.repo = repo;
            }

            @Aliases("title")
            public Mono<CommandResult> removeTitleFilter(PersistedFeedSubscription subscription, String regex) {
                var removed = subscription.getTitleRegexes().remove(regex);
                if (!removed) {
                    return embed("This filter hadn't already been applied");
                }

                repo.save(subscription);
                return embed("Removed filter %s", regex);
            }

            @Aliases({"description", "desc"})
            public Mono<CommandResult> removeDescriptionFilter(PersistedFeedSubscription subscription, String regex) {
                var removed = subscription.getDescriptionRegexes().remove(regex);
                if (!removed) {
                    return embed("This filter hadn't already been applied");
                }

                repo.save(subscription);
                return embed("Removed filter %s", regex);
            }
        }
    }
}
