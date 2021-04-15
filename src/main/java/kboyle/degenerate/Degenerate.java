package kboyle.degenerate;

import discord4j.core.GatewayDiscordClient;
import kboyle.degenerate.configuration.DegenerateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableConfigurationProperties(DegenerateConfig.class)
@EnableTransactionManagement
@EnableJpaRepositories
public class Degenerate implements CommandLineRunner {
    private final GatewayDiscordClient client;

    public static void main(String[] args) {
        SpringApplication.run(Degenerate.class, args);
    }

    @Autowired
    public Degenerate(GatewayDiscordClient client) {
        this.client = client;
    }

    @Override
    public void run(String... args) {
        client.onDisconnect().block();
    }
}
