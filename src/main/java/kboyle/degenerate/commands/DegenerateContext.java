package kboyle.degenerate.commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import kboyle.oktane.reactive.CommandContext;
import reactor.core.publisher.Mono;

public class DegenerateContext extends CommandContext {
    public final Message message;
    public final GatewayDiscordClient client;

    public DegenerateContext(ApplicationContextWrapper applicationContextWrapper, Message message) {
        super(applicationContextWrapper);
        this.message = message;
        this.client = message.getClient();
    }

    public Mono<Member> author() {
        return message.getAuthorAsMember();
    }

    public Mono<Guild> guild() {
        return message.getGuild();
    }

    public Mono<TextChannel> channel() {
        return message.getChannel().ofType(TextChannel.class);
    }
}
