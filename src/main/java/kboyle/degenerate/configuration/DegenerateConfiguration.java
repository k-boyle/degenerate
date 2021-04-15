package kboyle.degenerate.configuration;

import com.apptastic.rssreader.RssReader;
import discord4j.core.GatewayDiscordClient;
import kboyle.degenerate.persistence.dao.PersistedGuildRepository;
import kboyle.degenerate.services.PrefixService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DegenerateConfiguration {
    @Bean
    public PrefixService prefixService(PersistedGuildRepository repo, GatewayDiscordClient client) {
        return new PrefixService(repo, client.getApplicationInfo().block());
    }

    @Bean
    public RssReader rssReader() {
        return new RssReader();
    }
}
