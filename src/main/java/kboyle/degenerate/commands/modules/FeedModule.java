package kboyle.degenerate.commands.modules;

import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;
import discord4j.core.object.entity.channel.TextChannel;
import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
import kboyle.degenerate.persistence.dao.PersistedRssFeedRepository;
import kboyle.degenerate.persistence.dao.PersistedSubscriptionRepository;
import kboyle.degenerate.persistence.entities.PersistedFeedSubscription;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.oktane.reactive.module.annotations.Aliases;
import kboyle.oktane.reactive.module.annotations.Name;
import kboyle.oktane.reactive.module.annotations.Remainder;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Aliases({"feed", "f"})
@Name("Feed")
public class FeedModule extends DegenerateModule {
    private final PersistedGuildRepository guildRepo;
    private final PersistedRssFeedRepository feedRepo;
    private final RssReader rssReader;

    public FeedModule(PersistedGuildRepository guildRepo, PersistedRssFeedRepository feedRepo, RssReader rssReader) {
        this.guildRepo = guildRepo;
        this.feedRepo = feedRepo;
        this.rssReader = rssReader;
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

                if (subscribedFeeds.containsKey(feed)) {
                    return embed("You are already subscribed to this feed");
                }

                return Mono.fromFuture(rssReader.readAsync(feed.getUrl()))
                    .flatMap(items -> {
                        var guilds = items.map(Item::getGuid)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toSet());

                        var subscription = new PersistedFeedSubscription(
                            feed,
                            new HashSet<>(),
                            new HashSet<>(),
                            channel.getId().asLong(),
                            guilds,
                            guild.getId().asLong()
                        );

                        subscribedFeeds.put(feed, subscription);
                        guildRepo.save(persistedGuild);

                        return embed("Subscribed to feed %s", feed.getUrl());
                    });
            });
    }

    @Aliases({"unsubscribe", "unsub"})
    public Mono<CommandResult> unsubscribe(PersistedFeedSubscription subscription) {
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
    @Name("Filter")
    public static class FeedFilterModule extends DegenerateModule {
        // todo list

        @Aliases({"add", "a"})
        @Name("Add Filter")
        public static class FeedAddFilterModule extends DegenerateModule {
            private final PersistedSubscriptionRepository repo;

            public FeedAddFilterModule(PersistedSubscriptionRepository repo) {
                this.repo = repo;
            }

            @Aliases("title")
            public Mono<CommandResult> addTitleFilter(PersistedFeedSubscription subscription, @Remainder String regex) {
                var added = subscription.getTitleRegexes().add(Pattern.compile(regex));
                if (!added) {
                    return embed("This filter had already been applied");
                }

                repo.save(subscription);
                return embed("Added filter %s", regex);
            }

            @Aliases({"description", "desc"})
            public Mono<CommandResult> addDescriptionFilter(PersistedFeedSubscription subscription, @Remainder String regex) {
                var added = subscription.getDescriptionRegexes().add(Pattern.compile(regex));
                if (!added) {
                    return embed("This filter had already been applied");
                }

                repo.save(subscription);
                return embed("Added filter %s", regex);
            }
        }

        @Aliases({"remove", "rm", "r"})
        @Name("Remove Filter")
        public static class FeedRemoveFilterModule extends DegenerateModule {
            private final PersistedSubscriptionRepository repo;

            public FeedRemoveFilterModule(PersistedSubscriptionRepository repo) {
                this.repo = repo;
            }

            @Aliases("title")
            public Mono<CommandResult> removeTitleFilter(PersistedFeedSubscription subscription, @Remainder String regex) {
                var removed = subscription.getTitleRegexes().remove(regex);
                if (!removed) {
                    return embed("This filter hadn't already been applied");
                }

                repo.save(subscription);
                return embed("Removed filter %s", regex);
            }

            @Aliases({"description", "desc"})
            public Mono<CommandResult> removeDescriptionFilter(PersistedFeedSubscription subscription, @Remainder String regex) {
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
