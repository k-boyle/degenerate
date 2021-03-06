package kboyle.degenerate.configuration;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Discord4JConfiguration {
    @Bean
    public GatewayDiscordClient gatewayDiscordClient(DegenerateConfig config) {
        return DiscordClientBuilder.create(config.getDiscord().getToken())
            .build()
            .login()
            .block();
    }

    @Bean
    public CommandLineRunner commandLineRunner(GatewayDiscordClient client) {
        return ctx -> client.onDisconnect().block();
    }
}
