package kboyle.degenerate.configuration;

import com.google.common.reflect.TypeToken;
import discord4j.core.object.entity.channel.TextChannel;
import kboyle.degenerate.commands.ApplicationContextWrapper;
import kboyle.degenerate.commands.DegenerateContext;
import kboyle.degenerate.commands.parsers.*;
import kboyle.degenerate.persistence.entities.PersistedFeedSubscription;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.oktane.reactive.ReactiveCommandHandler;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.module.ReactiveModule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
            // todo use spring to get parsers
            .withTypeParser(TextChannel.class, new TextChannelTypeParser())
            .withTypeParser(PersistedRssFeed.class, new RssFeedTypeParser())
            .withTypeParser(PersistedFeedSubscription.class, new RssFeedSubscriptionTypeParser())
            .withTypeParser(ReactiveModule.class, new ModuleTypeParser())
            .withTypeParser(getCommandListType(), new CommandsTypeParser())
            .build();
    }

    @SuppressWarnings("unchecked")
    private Class<List<ReactiveCommand>> getCommandListType() {
        return (Class<List<ReactiveCommand>>) new TypeToken<List<ReactiveCommand>>() { }.getRawType();
    }
}
