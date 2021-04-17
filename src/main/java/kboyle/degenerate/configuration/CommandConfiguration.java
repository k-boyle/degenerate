package kboyle.degenerate.configuration;

import discord4j.core.object.entity.channel.TextChannel;
import kboyle.degenerate.commands.ApplicationContextWrapper;
import kboyle.degenerate.commands.DegenerateContext;
import kboyle.degenerate.commands.parsers.RssFeedSubscriptionTypeParser;
import kboyle.degenerate.commands.parsers.RssFeedTypeParser;
import kboyle.degenerate.commands.parsers.TextChannelTypeParser;
import kboyle.degenerate.persistence.entities.PersistedFeedSubscription;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.oktane.reactive.ReactiveCommandHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandConfiguration {
    @Bean
    public ApplicationContextWrapper applicationContextWrapper(ApplicationContext applicationContext) {
        return new ApplicationContextWrapper(applicationContext);
    }

    @Bean
    public ReactiveCommandHandler<DegenerateContext> commandHandler() {
        return ReactiveCommandHandler.<DegenerateContext>builder()
            .withModules(DegenerateContext.class)
            .withTypeParser(TextChannel.class, new TextChannelTypeParser())
            .withTypeParser(PersistedRssFeed.class, new RssFeedTypeParser())
            .withTypeParser(PersistedFeedSubscription.class, new RssFeedSubscriptionTypeParser())
            .build();
    }
}
