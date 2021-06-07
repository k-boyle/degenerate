package kboyle.degenerate.configuration;

import kboyle.degenerate.commands.modules.FeedModule;
import kboyle.degenerate.services.PrefixService;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.DiscordCommandHandler;
import kboyle.oktane.discord4j.prefix.DiscordPrefixHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        );
    }

    @Bean
    public DiscordPrefixHandler prefixHandler(PrefixService prefixService) {
        return context -> context.guild().map(prefixService::getPrefixes);
    }
}
