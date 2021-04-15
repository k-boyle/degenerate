package kboyle.degenerate.commands.results;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.AllowedMentions;
import kboyle.degenerate.Constants;
import kboyle.oktane.reactive.module.ReactiveCommand;

public class DiscordEmbedResult extends DiscordResult {
    private final String content;
    private final Snowflake messageRef;

    public DiscordEmbedResult(ReactiveCommand command, TextChannel channel, String content, Snowflake messageRef) {
        super(command, channel);
        this.content = content;
        this.messageRef = messageRef;
    }

    @Override
    public void applySpec(MessageCreateSpec spec) {
        spec.setEmbed(embedSpec -> embedSpec.setDescription(content).setColor(Constants.DEGENERATE_COLOUR))
            .setMessageReference(messageRef)
            .setAllowedMentions(AllowedMentions.suppressAll());
    }
}
