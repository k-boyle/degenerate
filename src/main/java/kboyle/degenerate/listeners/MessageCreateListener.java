package kboyle.degenerate.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.AllowedMentions;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.results.ExceptionResult;
import kboyle.oktane.core.results.search.CommandNotFoundResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.DiscordCommandHandler;
import kboyle.oktane.discord4j.results.DiscordResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MessageCreateListener extends DiscordListener<MessageCreateEvent> {
    private final DiscordCommandHandler<DiscordCommandContext> commandHandler;
    private final BeanProvider beanProvider;

    public MessageCreateListener(
            GatewayDiscordClient gatewayDiscordClient,
            DiscordCommandHandler<DiscordCommandContext> commandHandler,
            BeanProvider beanProvider) {
        super(MessageCreateEvent.class, gatewayDiscordClient);
        this.commandHandler = commandHandler;
        this.beanProvider = beanProvider;
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        return Mono.just(event.getMessage())
            .filter(this::notBot)
            .flatMap(message ->
                message.getChannel()
                    .ofType(TextChannel.class)
                    .flatMap(channel -> executeCommand(message, channel))
            )
            .then();
    }

    private boolean notBot(Message message) {
        return !message.getAuthor().map(User::isBot).orElse(false);
    }

    private Mono<Void> executeCommand(Message message, TextChannel channel) {
        logger.info("Executing {}", message.getContent());

        var context = new DiscordCommandContext(message, beanProvider);

        return commandHandler.execute(message.getContent(), context)
            .flatMap(result -> {
                logger.info("Got result {}", result);

                if (result instanceof DiscordResult discordResult) {
                    return discordResult.execute();
                } else if (!(result instanceof CommandNotFoundResult) && !result.success()) {
                    if (result instanceof ExceptionResult exceptionResult) {
                        logger.error("An exception was thrown", exceptionResult.exception());
                    }

                    return channel.createMessage(result.toString()).then();
                }

                return Mono.empty();
            })
            .doOnError(ex -> logger.error("An exception occurred when trying to execute the command", ex))
            .onErrorResume(ex ->
                channel.createMessage(spec ->
                    spec.setMessageReference(context.message().getId())
                        .setAllowedMentions(AllowedMentions.suppressAll())
                        .setContent("An exception was thrown when trying to execute the command, ping the dev")
                )
                .doOnError(ohno -> logger.error("Couldn't send a message to this channel", ohno))
                .onErrorResume(swallow -> Mono.empty())
                .then()
            );
    }
}
