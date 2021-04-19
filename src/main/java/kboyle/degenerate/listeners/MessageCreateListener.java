package kboyle.degenerate.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.AllowedMentions;
import kboyle.degenerate.commands.ApplicationContextWrapper;
import kboyle.degenerate.commands.DegenerateContext;
import kboyle.degenerate.commands.results.DiscordResult;
import kboyle.degenerate.services.PrefixService;
import kboyle.oktane.reactive.ReactiveCommandHandler;
import kboyle.oktane.reactive.results.ExceptionResult;
import kboyle.oktane.reactive.results.search.CommandNotFoundResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MessageCreateListener extends DiscordListener<MessageCreateEvent> {
    private final ReactiveCommandHandler<DegenerateContext> commandHandler;
    private final ApplicationContextWrapper applicationContextWrapper;
    private final PrefixService prefixService;

    public MessageCreateListener(
        GatewayDiscordClient gatewayDiscordClient,
        ReactiveCommandHandler<DegenerateContext> commandHandler,
        ApplicationContextWrapper applicationContextWrapper,
        PrefixService prefixService) {
        super(MessageCreateEvent.class, gatewayDiscordClient);
        this.commandHandler = commandHandler;
        this.applicationContextWrapper = applicationContextWrapper;
        this.prefixService = prefixService;
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        return Mono.just(event.getMessage())
            .filter(this::notBot)
            .flatMap(message ->
                message.getChannel()
                    .ofType(TextChannel.class)
                    .flatMap(channel ->
                        prefixService.hasPrefix(message)
                            .flatMap(command -> executeCommand(message, channel, command))
                    )
            )
            .then();
    }

    private boolean notBot(Message message) {
        return !message.getAuthor().map(User::isBot).orElse(false);
    }

    private Mono<Void> executeCommand(Message message, TextChannel channel, String command) {
        logger.info("Executing {}", command);

        var context = new DegenerateContext(
            applicationContextWrapper,
            message,
            channel
        );

        return commandHandler.execute(command, context)
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
            .doOnError(ex -> logger.error("An exception was thrown when trying to execute a command", ex))
            .onErrorResume(ex ->
                context.channel.createMessage(spec ->
                    spec.setMessageReference(context.message.getId())
                        .setAllowedMentions(AllowedMentions.suppressAll())
                        .setContent("An exception was thrown when trying to execute the command, ping the dev")
                )
                .doOnError(ohno -> logger.error("Couldn't send a message to this channel", ohno))
                .onErrorResume(swallow -> Mono.empty())
                .then()
            );
    }
}
