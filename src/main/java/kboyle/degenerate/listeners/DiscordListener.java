package kboyle.degenerate.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public abstract class DiscordListener<T extends Event> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public DiscordListener(Class<T> cl, GatewayDiscordClient gatewayDiscordClient) {
        logger.info("Adding listener for {}", cl);

        gatewayDiscordClient.getEventDispatcher()
            .on(cl)
            .flatMap(this::handle)
            .subscribe(v -> {}, ex -> logger.error("An exception was thrown when handling an event", ex));
    }

    public abstract Mono<Void> handle(T event);
}
