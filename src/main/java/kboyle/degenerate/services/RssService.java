package kboyle.degenerate.services;

import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import kboyle.degenerate.configuration.DegenerateConfig;
import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
import kboyle.degenerate.persistence.entities.PersistedFeedSubscription;
import kboyle.degenerate.persistence.entities.PersistedGuild;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.degenerate.wrapper.PatternWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static kboyle.degenerate.Constants.DEGENERATE_COLOUR;
import static kboyle.degenerate.Markdown.URL;

@Component
public class RssService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GatewayDiscordClient client;
    private final PersistedGuildRepository guildRepo;
    private final DegenerateConfig degenerateConfig;
    private final RssReader rssReader;

    @Autowired
    public RssService(
            GatewayDiscordClient client,
            PersistedGuildRepository guildRepo,
            DegenerateConfig degenerateConfig,
            RssReader rssReader) {
        this.client = client;
        this.guildRepo = guildRepo;
        this.degenerateConfig = degenerateConfig;
        this.rssReader = rssReader;

        if (degenerateConfig.getFeed().isEnabled()) {
            rssLoop().subscribe();
        }
    }

    // todo this needs a LOT of cleanup
    private Mono<Void> rssLoop() {
        return Flux.fromStream(() -> getSubscriptionsByFeed().asMap().entrySet().stream())
            .delaySequence(degenerateConfig.getFeed().getPollingRate())
            .publishOn(Schedulers.boundedElastic())
            .flatMap(entry -> {
                var feed = entry.getKey();
                var guildWithSubscriptions = entry.getValue();

                return Mono.fromFuture(rssReader.readAsync(feed.getUrl()))
                    .flatMapMany(Flux::fromStream)
                    .collectList()
                    .flatMapMany(items -> {
                        var fetchedItemByGuid = items.stream().filter(item -> item.getGuid().isPresent())
                            .collect(Collectors.toMap(item -> item.getGuid().get(), Function.identity()));

                        return postNewItems(guildWithSubscriptions, fetchedItemByGuid);
                    });
            })
            .doOnNext(message -> logger.info("Sending rss notification to {}", message.getChannelId()))
            .repeat()
            .doOnError(ex -> logger.error("An error was thrown whilst trying to poll feeds", ex))
            .onErrorResume(ex -> Mono.empty())
            .then();
    }

    private Flux<Message> postNewItems(
            Collection<GuildWithSubscription> guildWithSubscriptions,
            Map<String, Item> fetchedItemByGuid) {
        return Flux.fromStream(guildWithSubscriptions.stream())
            .flatMap(guildWithSubscription ->  postNewItems(fetchedItemByGuid, guildWithSubscription));
    }

    private Flux<Message> postNewItems(Map<String, Item> fetchedItemByGuid, GuildWithSubscription guildWithSubscription) {
        var persistedGuild = guildWithSubscription.guild;
        var subscription = guildWithSubscription.subscription;

        var fetchedGuids = fetchedItemByGuid.keySet();
        logger.debug("Got {} items", fetchedGuids.size());

        logger.debug("Checking diffs for {}", subscription.getChannelId());
        var lastGuids = subscription.getLastGuids();
        var newItems = getNewItems(fetchedItemByGuid, subscription, fetchedGuids, lastGuids);

        logger.debug("Found {} matching items in {}", newItems.size(), subscription.getChannelId());

        if (newItems.isEmpty()) {
            return Flux.empty();
        }

        saveNewItems(persistedGuild, lastGuids, newItems);

        return Flux.fromIterable(newItems)
            .flatMap(item ->
                client.getChannelById(Snowflake.of(subscription.getChannelId()))
                    .ofType(TextChannel.class)
                    .flatMap(channel -> channel.createMessage(spec -> createMessage(spec, subscription, item)))
            );
    }

    private List<Item> getNewItems(
            Map<String, Item> itemByGuid,
            PersistedFeedSubscription subscription,
            Set<String> fetchedGuids,
            Set<String> lastGuids) {
        var diff = Sets.difference(fetchedGuids, lastGuids);
        // todo swap diff and delete
        logger.debug("Found {} diffs for {}", diff.size(), subscription.getChannelId());

        if (diff.isEmpty()) {
            return List.of();
        }

        logger.debug("Checking for matching items in {}", subscription.getChannelId());

        return diff.stream()
            .map(itemByGuid::get)
            .filter(item -> itemMatches(item, subscription))
            .collect(Collectors.toList());
    }

    private void saveNewItems(PersistedGuild persistedGuild, Set<String> lastGuids, List<Item> newItems) {
        newItems.forEach(item -> item.getGuid().ifPresent(lastGuids::add));
        guildRepo.save(persistedGuild);
        logger.debug("Updated {} with {} new items", persistedGuild.getId(), newItems.size());
    }

    private boolean itemMatches(Item item, PersistedFeedSubscription subscription) {
        return itemMatches(item.getTitle(), subscription.getTitleRegexes(), false)
            && itemMatches(item.getDescription(), subscription.getDescriptionRegexes(), true);
    }

    private Boolean itemMatches(Optional<String> item, Set<PatternWrapper> regexes, boolean fallback) {
        return item.map(str -> itemMatches(str, regexes)).orElse(fallback);
    }

    private static boolean itemMatches(String item, Set<PatternWrapper> regexes) {
        return regexes.isEmpty() || regexes.stream().anyMatch(regex -> regex.match(item));
    }

    private HashMultimap<PersistedRssFeed, GuildWithSubscription> getSubscriptionsByFeed() {
        return StreamSupport.stream(guildRepo.findAll().spliterator(), false)
            .<Map.Entry<PersistedRssFeed, GuildWithSubscription>>mapMulti((guild, downstream) ->
                guild.getSubscriptionByFeedUrl()
                    .forEach((feed, subscription) -> {
                        var entry = new SimpleEntry<>(
                            feed,
                            new GuildWithSubscription(guild, subscription)
                        );
                        downstream.accept(entry);
                    })
            )
            .collect(
                HashMultimap::create,
                (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                (left, right) -> left.putAll(right)
            );
    }

    private void createMessage(MessageCreateSpec spec, PersistedFeedSubscription subscription, Item item) {
        spec.setEmbed(embed -> {
            var description = item.getTitle()
                .flatMap(title -> item.getLink().map(url -> URL.format("", title, url)))
                .orElse("There was no title/url, visit the site directly");

            embed.setColor(DEGENERATE_COLOUR)
                .setTitle("There is a new post on " + subscription.getFeed().getName())
                .setDescription(description);
        });
    }

    private static record GuildWithSubscription(PersistedGuild guild, PersistedFeedSubscription subscription) {
    }
}
