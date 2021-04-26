package kboyle.degenerate.commands.results;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.AllowedMentions;
import kboyle.oktane.core.module.Command;
import reactor.core.publisher.Mono;

public class DiscordReplyResult extends DiscordResult {
    private final TextChannel channel;
    private final String message;
    private final Snowflake messageRef;

    public DiscordReplyResult(Command command, TextChannel channel, String message, Snowflake messageRef) {
        super(command);
        this.channel = channel;
        this.message = message;
        this.messageRef = messageRef;
    }

    @Override
    public Mono<Void> execute() {
        return channel.createMessage(this::applySpec).then();
    }

    private void applySpec(MessageCreateSpec spec) {
        spec.setContent(message)
            .setMessageReference(messageRef)
            .setAllowedMentions(AllowedMentions.suppressAll());
    }
}
