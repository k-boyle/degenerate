package kboyle.degenerate.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReadyListener extends DiscordListener<ReadyEvent> {
    public ReadyListener(GatewayDiscordClient gatewayDiscordClient) {
        super(ReadyEvent.class, gatewayDiscordClient);
    }

    @Override
    public Mono<Void> handle(ReadyEvent event) {
        logger.info("Degenerate is ready");
        return Mono.empty();
    }
}
