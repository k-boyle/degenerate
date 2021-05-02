package kboyle.degenerate.services;

import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.dto.tweet.Tweet;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import kboyle.degenerate.Utils;
import kboyle.degenerate.configuration.DegenerateConfig;
import kboyle.degenerate.persistence.dao.PersistedTwitterSubscriptionRepository;
import kboyle.degenerate.persistence.entities.PersistedTwitterSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class TwitterService {
    private static final int TWEET_HISTORY = 10;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GatewayDiscordClient client;
    private final PersistedTwitterSubscriptionRepository subscriptionRepo;
    private final DegenerateConfig degenerateConfig;
    private final TwitterClient twitterClient;

    @Autowired
    public TwitterService(
            GatewayDiscordClient client,
            PersistedTwitterSubscriptionRepository subscriptionRepo,
            DegenerateConfig degenerateConfig,
            TwitterClient twitterClient) {
        this.client = client;
        this.subscriptionRepo = subscriptionRepo;
        this.degenerateConfig = degenerateConfig;
        this.twitterClient = twitterClient;

        if (degenerateConfig.getTwitter().isEnabled()) {
            twitterLoop().subscribe();
        }
    }

    private Mono<Void> twitterLoop() {
        return Flux.fromStream(() -> getSubscriptionsById().asMap().entrySet().stream())
            .delaySequence(degenerateConfig.getTwitter().getPollingRate())
            .publishOn(Schedulers.boundedElastic())
            .flatMap(entry -> {
                var id = entry.getKey();
                var subscriptions = entry.getValue();

                var timeline = twitterClient.getUserTimeline(id, TWEET_HISTORY)
                    .stream()
                    .collect(Collectors.toMap(Tweet::getId, Function.identity()));
                var newTweetIds = timeline.keySet();

                logger.info("Found {} tweets", newTweetIds.size());
                return Flux.fromStream(subscriptions.stream())
                    .flatMap(subscription -> {
                        var diff = List.copyOf(Sets.difference(newTweetIds, subscription.getLastIds()));
                        logger.info("Found {} new tweets for {}", diff.size(), subscription.getGuild().getId());

                        if (diff.isEmpty()) {
                            return Flux.empty();
                        }

                        subscription.getLastIds().addAll(diff);
                        subscriptionRepo.save(subscription);

                        return Flux.fromIterable(diff)
                            .flatMap(newId ->
                                client.getChannelById(Snowflake.of(subscription.getChannelId()))
                                    .ofType(TextChannel.class)
                                    .flatMap(channel -> channel.createMessage(messageSpec -> {
                                        var tweet = timeline.get(newId);
                                        var tweetUrl = Utils.getTweetUrl(tweet.getAuthorId(), tweet.getId());
                                        messageSpec.setContent(tweetUrl);
                                    }))
                            );
                    });
            })
            .doOnNext(message -> logger.info("Sending twitter notification to {}", message.getChannelId()))
            .repeat()
            .doOnError(ex -> logger.error("An error was thrown whilst trying to poll feeds", ex))
            .onErrorResume(ex -> Mono.empty())
            .then();
    }

    private HashMultimap<String, PersistedTwitterSubscription> getSubscriptionsById() {
        return StreamSupport.stream(subscriptionRepo.findAll().spliterator(), false)
            .<PersistedTwitterSubscription>mapMulti((subscription, downstream) -> downstream.accept(subscription))
            .collect(
                HashMultimap::create,
                (map, subscription) -> map.put(subscription.getId(), subscription),
                (left, right) -> left.putAll(right)
            );
    }
}
