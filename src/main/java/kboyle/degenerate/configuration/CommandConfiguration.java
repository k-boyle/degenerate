package kboyle.degenerate.configuration;

import com.github.redouane59.twitter.dto.user.UserV2;
import com.google.common.reflect.TypeToken;
import kboyle.degenerate.commands.modules.FeedModule;
import kboyle.degenerate.commands.parsers.*;
import kboyle.degenerate.persistence.entities.PersistedFeedSubscription;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.degenerate.persistence.entities.PersistedTwitterSubscription;
import kboyle.degenerate.services.PrefixService;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.DiscordCommandHandler;
import kboyle.oktane.discord4j.prefix.DiscordPrefixHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommandConfiguration {
    @Bean
    public BeanProvider beanProvider(ApplicationContext applicationContext) {
        return applicationContext::getBean;
    }

    @Bean
    public DiscordCommandHandler<DiscordCommandContext> commandHandler(DiscordPrefixHandler prefixHandler) {
        return DiscordCommandHandler.create(builder ->
            builder.withModules(FeedModule.class)
                .withPrefixHandler(prefixHandler)
                .withTypeParser(PersistedRssFeed.class, new RssFeedTypeParser())
                .withTypeParser(PersistedFeedSubscription.class, new RssFeedSubscriptionTypeParser())
                .withTypeParser(CommandModule.class, new ModuleTypeParser())
                .withTypeParser(UserV2.class, new TwitterUserTypeParser())
                .withTypeParser(PersistedTwitterSubscription.class, new TwitterSubscriptionTypeParser())
                .withTypeParser(getCommandListType(), new CommandsTypeParser())
        );
    }

    @SuppressWarnings("unchecked")
    private Class<List<Command>> getCommandListType() {
        return (Class<List<Command>>) new TypeToken<List<Command>>() {
        }.getRawType();
    }

    @Bean
    public DiscordPrefixHandler prefixHandler(PrefixService prefixService) {
        return context -> context.guild().map(prefixService::getPrefixes);
    }
}
