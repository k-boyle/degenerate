package kboyle.degenerate.commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import kboyle.oktane.core.CommandContext;
import reactor.core.publisher.Mono;

public class DegenerateContext extends CommandContext {
    public final Message message;
    public final GatewayDiscordClient client;
    public final TextChannel channel;

    public DegenerateContext(ApplicationContextWrapper applicationContextWrapper, Message message, TextChannel channel) {
        super(applicationContextWrapper);

        this.message = message;
        this.client = message.getClient();
        this.channel = channel;
    }

    public Mono<Member> author() {
        return message.getAuthorAsMember();
    }

    public Mono<Guild> guild() {
        return message.getGuild();
    }
}
