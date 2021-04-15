package kboyle.degenerate.commands.results;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.AllowedMentions;
import kboyle.oktane.reactive.module.ReactiveCommand;

public class DiscordReplyResult extends DiscordResult {
    private final String message;
    private final Snowflake messageRef;

    public DiscordReplyResult(ReactiveCommand command, TextChannel channel, String message, Snowflake messageRef) {
        super(command, channel);
        this.message = message;
        this.messageRef = messageRef;
    }
    
    @Override
    public void applySpec(MessageCreateSpec spec) {
        spec.setContent(message)
            .setMessageReference(messageRef)
            .setAllowedMentions(AllowedMentions.suppressAll());
    }
}
