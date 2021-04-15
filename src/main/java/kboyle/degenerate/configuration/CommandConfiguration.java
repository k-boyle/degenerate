package kboyle.degenerate.configuration;

import kboyle.degenerate.commands.ApplicationContextWrapper;
import kboyle.degenerate.commands.DegenerateContext;
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
            .build();
    }
}
