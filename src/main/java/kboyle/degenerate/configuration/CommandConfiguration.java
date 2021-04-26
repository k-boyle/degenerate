package kboyle.degenerate.configuration;

import com.google.common.reflect.TypeToken;
import discord4j.core.object.entity.channel.TextChannel;
import kboyle.degenerate.commands.ApplicationContextWrapper;
import kboyle.degenerate.commands.DegenerateContext;
import kboyle.degenerate.commands.modules.FeedModule;
import kboyle.degenerate.commands.modules.MiscModule;
import kboyle.degenerate.commands.modules.PrefixModule;
import kboyle.degenerate.commands.parsers.*;
import kboyle.degenerate.persistence.entities.PersistedFeedSubscription;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
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
    public CommandHandler<DegenerateContext> commandHandler() {
        return CommandHandler.<DegenerateContext>builder()
            .withModule(MiscModule.class)
            .withModule(FeedModule.class)
            .withModule(PrefixModule.class)
            // todo use spring to get parsers
            .withTypeParser(TextChannel.class, new TextChannelTypeParser())
            .withTypeParser(PersistedRssFeed.class, new RssFeedTypeParser())
            .withTypeParser(PersistedFeedSubscription.class, new RssFeedSubscriptionTypeParser())
            .withTypeParser(CommandModule.class, new ModuleTypeParser())
            .withTypeParser(getCommandListType(), new CommandsTypeParser())
            .build();
    }

    @SuppressWarnings("unchecked")
    private Class<List<Command>> getCommandListType() {
        return (Class<List<Command>>) new TypeToken<List<Command>>() { }.getRawType();
    }
}
