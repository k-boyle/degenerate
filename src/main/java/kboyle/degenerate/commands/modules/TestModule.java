package kboyle.degenerate.commands.modules;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.TextChannel;
import kboyle.degenerate.commands.DegenerateModule;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.results.command.CommandResult;

@Aliases("test")
public class TestModule extends DegenerateModule {
    @Aliases("channel")
    public CommandResult channel(TextChannel channel) {
        return reply(channel.getId().toString());
    }

    @Aliases("role")
    public CommandResult role(Role role) {
        return reply(role.getId().toString());
    }

    @Aliases("member")
    public CommandResult member(Member member) {
        return reply(member.getId().toString());
    }
}
