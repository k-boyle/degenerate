package kboyle.degenerate.commands.results;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.AllowedMentions;
import kboyle.oktane.core.module.Command;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class DiscordCreateMessageResult extends DiscordResult {
    private final Snowflake messageRef;
    private final TextChannel channel;
    private final Consumer<MessageCreateSpec> specConsumer;

    public DiscordCreateMessageResult(
        Command command,
            Snowflake messageRef,
            TextChannel channel,
            Consumer<MessageCreateSpec> specConsumer) {
        super(command);
        this.messageRef = messageRef;
        this.channel = channel;
        this.specConsumer = specConsumer;
    }

    @Override
    public Mono<Void> execute() {
        var message = channel.createMessage(spec -> {
            spec.setMessageReference(messageRef)
                .setAllowedMentions(AllowedMentions.suppressAll());

            specConsumer.accept(spec);
        });

        return message.then();
    }
}
