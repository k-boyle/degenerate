package kboyle.degenerate.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public abstract class DiscordListener<T extends Event> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<T> eventType;

    public DiscordListener(Class<T> eventType, GatewayDiscordClient gatewayDiscordClient) {
        this.eventType = eventType;
        logger.info("Adding listener for {}", eventType);

        gatewayDiscordClient.getEventDispatcher()
            .on(eventType)
            .flatMap(this::handle)
            .doOnError(this::onError)
            .onErrorResume(ex -> Mono.empty())
            .subscribe();
    }

    public void onError(Throwable ex) {
        logger.error("An error was thrown when trying to execute event {}", eventType, ex);
    }

    public abstract Mono<Void> handle(T event);
}
