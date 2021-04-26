package kboyle.degenerate.commands.modules;

import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;
import discord4j.core.object.entity.channel.TextChannel;
import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.commands.preconditions.RequireBotOwner;
import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
import kboyle.degenerate.persistence.dao.PersistedRssFeedRepository;
import kboyle.degenerate.persistence.dao.PersistedSubscriptionRepository;
import kboyle.degenerate.persistence.entities.PersistedFeedSubscription;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.degenerate.wrapper.PatternWrapper;
import kboyle.oktane.core.module.annotations.*;
import kboyle.oktane.core.processor.OktaneModule;
import kboyle.oktane.core.results.command.CommandResult;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Aliases({"feed", "f"})
@Name("Feed")
@Description("Commands to manage your feed subscriptions and feed filters")
@OktaneModule
public class FeedModule extends DegenerateModule {
    private final PersistedGuildRepository guildRepo;
    private final PersistedRssFeedRepository feedRepo;
    private final RssReader rssReader;

    public FeedModule(PersistedGuildRepository guildRepo, PersistedRssFeedRepository feedRepo, RssReader rssReader) {
        this.guildRepo = guildRepo;
        this.feedRepo = feedRepo;
        this.rssReader = rssReader;
    }

    @SneakyThrows
    @Aliases("preview")
    public CommandResult preview(String url) {
        var feed = rssReader.read(url);
        return nop();
    }

    @Aliases({"add", "a"})
    @Require(precondition = RequireBotOwner.class)
    public Mono<CommandResult> addFeed(String name, String url) {
        return Mono.justOrEmpty(feedRepo.findById(url))
            .map(feed -> embed("Feed was already added"))
            .switchIfEmpty(
                Mono.fromFuture(() -> rssReader.readAsync(url))
                    .map(items -> {
                        feedRepo.save(new PersistedRssFeed(name, url));
                        return embed("Added feed %s", name);
                    })
            );
    }

    @Aliases({"remove", "rm", "r"})
    @Require(precondition = RequireBotOwner.class)
    public CommandResult removeFeed(@Remainder PersistedRssFeed feed) {
        feedRepo.delete(feed);
        return embed("Deleted feed %s", feed.getName());
    }

    @Aliases({"subscribe", "sub"})
    public Mono<CommandResult> subscribe(@Remainder PersistedRssFeed feed) {
        return subscribe(context().channel, feed);
    }

    @Aliases({"subscribe", "sub"})
    public Mono<CommandResult> subscribe(TextChannel channel, @Remainder PersistedRssFeed feed) {
        return context().guild()
            .flatMap(guild -> {
                var persistedGuild = guildRepo.get(guild);
                var subscribedFeeds = persistedGuild.getSubscriptionByFeedUrl();

                if (subscribedFeeds.containsKey(feed)) {
                    return embed("You are already subscribed to %s", feed.getName()).mono();
                }

                return Mono.fromFuture(rssReader.readAsync(feed.getUrl()))
                    .map(items -> {
                        var guids = items.map(Item::getGuid)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toSet());

                        var subscription = new PersistedFeedSubscription(
                            feed,
                            new HashSet<>(),
                            new HashSet<>(),
                            channel.getId().asLong(),
                            guids,
                            guild.getId().asLong()
                        );

                        subscribedFeeds.put(feed, subscription);
                        guildRepo.save(persistedGuild);

                        return embed("Subscribed to feed %s", feed.getName());
                    });
            });
    }

    @Aliases({"unsubscribe", "unsub"})
    public Mono<CommandResult> unsubscribe(@Remainder PersistedFeedSubscription subscription) {
        return context().guild()
            .map(guild -> {
                var persistedGuild = guildRepo.get(guild);
                var removed = persistedGuild.getSubscriptionByFeedUrl().remove(subscription.getFeed());

                if (removed == null) {
                    return embed("You aren't subscribed to this feed");
                }

                guildRepo.save(persistedGuild);
                return embed("Unsubscribed from feed %s", removed.getFeed().getName());
            });
    }

    @Aliases({"list", "l", "available"})
    public CommandResult listFeeds() {
        var sb = new StringJoiner("\n");
        for (var feed : feedRepo.findAll()) {
            sb.add(feed.getName());
        }

        return embed(sb.length() == 0 ? "No available feeds" : sb.toString());
    }

    @Aliases({"subscribed", "subbed"})
    public Mono<CommandResult> subbedFeeds() {
        return context().guild()
            .map(guild -> {
                var persistedGuild = guildRepo.get(guild);
                var joiner = new StringJoiner("\n");
                for (var persistedRssFeed : persistedGuild.getSubscriptionByFeedUrl().keySet()) {
                    joiner.add(persistedRssFeed.getName());
                }

                return joiner.length() == 0
                    ? embed("This guild isn't subscribed to any feeds")
                    : embed(joiner.toString());
            });
    }

    @Aliases({"filter", "f"})
    @Name("Filter")
    @OktaneModule
    public static class FeedFilterModule extends DegenerateModule {
        @Aliases({"list", "l"})
        public CommandResult listFilters(@Remainder PersistedFeedSubscription subscription) {
            var sb = new StringJoiner("\n");

            for (var titleRegex : subscription.getTitleRegexes()) {
                sb.add("title: ");
                sb.add(titleRegex.regex());
            }

            for (var descriptionRegex : subscription.getDescriptionRegexes()) {
                sb.add("description: ");
                sb.add(descriptionRegex.regex());
            }

            return embed(sb.length() == 0 ? "No available feeds" : sb.toString());
        }

        @Aliases({"add", "a"})
        @Name("Add Filter")
        @OktaneModule
        public static class FeedAddFilterModule extends DegenerateModule {
            private final PersistedSubscriptionRepository repo;

            public FeedAddFilterModule(PersistedSubscriptionRepository repo) {
                this.repo = repo;
            }

            @Aliases("title")
            public CommandResult addTitleFilter(PersistedFeedSubscription subscription, @Remainder String regex) {
                var added = subscription.getTitleRegexes().add(new PatternWrapper(regex));
                if (!added) {
                    return embed("This filter had already been applied");
                }

                repo.save(subscription);
                return embed("Added filter %s to %s", regex, subscription.getFeed().getName());
            }

            @Aliases({"description", "desc"})
            public CommandResult addDescriptionFilter(PersistedFeedSubscription subscription, @Remainder String regex) {
                var added = subscription.getDescriptionRegexes().add(new PatternWrapper(regex));
                if (!added) {
                    return embed("This filter had already been applied");
                }

                repo.save(subscription);
                return embed("Added filter %s to %s", regex, subscription.getFeed().getName());
            }
        }

        @Aliases({"remove", "rm", "r"})
        @Name("Remove Filter")
        @OktaneModule
        public static class FeedRemoveFilterModule extends DegenerateModule {
            private final PersistedSubscriptionRepository repo;

            public FeedRemoveFilterModule(PersistedSubscriptionRepository repo) {
                this.repo = repo;
            }

            @Aliases("title")
            public CommandResult removeTitleFilter(PersistedFeedSubscription subscription, @Remainder String regex) {
                var removed = subscription.getTitleRegexes().remove(new PatternWrapper(regex));
                if (!removed) {
                    return embed("This filter hadn't already been applied");
                }

                repo.save(subscription);
                return embed("Removed filter %s from %s", regex, subscription.getFeed().getName());
            }

            @Aliases({"description", "desc"})
            public CommandResult removeDescriptionFilter(PersistedFeedSubscription subscription, @Remainder String regex) {
                var removed = subscription.getDescriptionRegexes().remove(new PatternWrapper(regex));
                if (!removed) {
                    return embed("This filter hadn't already been applied");
                }

                repo.save(subscription);
                return embed("Removed filter %s from %s", regex, subscription.getFeed().getName());
            }
        }
    }
}
